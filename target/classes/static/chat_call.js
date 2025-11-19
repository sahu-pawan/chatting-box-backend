const usernameIn = document.getElementById('username');
const peernameIn = document.getElementById('peername');
const joinBtn = document.getElementById('joinBtn');
const leaveBtn = document.getElementById('leaveBtn');
const chatWindow = document.getElementById('chatWindow');
const chatInput = document.getElementById('chatInput');
const sendBtn = document.getElementById('sendBtn');
const findRandomBtn = document.getElementById('findRandomBtn');

const localVideo = document.getElementById('localVideo');
let remoteVideo = document.getElementById('remoteVideo');
const startCallBtn = document.getElementById('startCallBtn');
const hangupBtn = document.getElementById('hangupBtn');

let stompClient;
let roomId = null;
let clientId = 'c-' + Math.floor(Math.random()*1000000);
let username = null;
let peername = null;

let pc;
let localStream;
let isInitiator = false; // true when this client created the offer
let stompSock = null;


let chatSub = null;
let signalSub = null;


// Reconnect/backoff settings
let reconnectAttempts = 0;
const maxReconnectAttempts = 8;
const baseReconnectDelay = 1000; // ms
let stompConnected = false;

const iceConfig = { iceServers: [{ urls: 'stun:stun.l.google.com:19302' }] };

function appendChat(text, me) {
  const chatMessages = document.getElementById('chatMessages');
  if (!chatMessages) return;
  
  const messageDiv = document.createElement('div');
  messageDiv.className = 'message';
  messageDiv.textContent = text;
  
  // // Style for my messages vs received messages
  if (me) {
    // messageDiv.style.background = 'rgba(40, 167, 69, 0.9)'; // Green for my messages
    messageDiv.style.marginLeft = 'auto';
    messageDiv.style.marginRight = '0';
    messageDiv.style.textAlign = 'right';
  
  } else {
    // messageDiv.style.background = 'rgba(0, 132, 255, 0.9)'; // Blue for received messages
    messageDiv.style.marginLeft = '0';
    messageDiv.style.marginRight = 'auto';
    messageDiv.style.marginBottom = '0px';
   
  }
  
  chatMessages.appendChild(messageDiv);
  chatMessages.scrollTop = chatMessages.scrollHeight;
}

function ensureRemoteVideo() {
  if (!remoteVideo) {
    const container = document.querySelector('.half.left') || document.body;
    const v = document.createElement('video');
    v.id = 'remoteVideo';
    v.autoplay = true;
    v.playsInline = true;
    container.appendChild(v);
    remoteVideo = v;
  }
  return remoteVideo;
}

function connect() {
  console.log('Attempting to connect to WebSocket...');
  // create SockJS and STOMP client, attach close handler for reconnect
  stompSock = new SockJS('/ws');
  stompClient = Stomp.over(stompSock);
  // disable verbose debug in production
  if (stompClient) stompClient.debug = null;

  const onConnect = () => {
    console.log('WebSocket connected successfully');
    reconnectAttempts = 0;
    stompConnected = true;
    // appendChat('Connected to signaling server', false);
    // subscribe to room chat and signals when room known
    if (roomId) subscribeRoom();
    // matchmaking responses
    stompClient.subscribe('/topic/match.' + clientId, frame => {
      try {
        console.log('Received match response:', frame.body);
        const res = JSON.parse(frame.body);
        roomId = res.roomId;
        // appendChat('Matched with ' + res.peerClientId + ', room=' + roomId, false);
        subscribeRoom();
        // determine initiator
        if (clientId < res.peerClientId) {
          isInitiator = true; 
          console.log('I am the initiator, starting call...');
          startCall();
        } else {
          isInitiator = false;
          console.log('I am not the initiator, waiting for offer...');
        }
      } catch (e) { console.error('match frame parse error', e); }
    });
  };

  const onError = (err) => {
    console.error('STOMP connection error', err);
    stompConnected = false;
    appendChat('Signaling connection error, will attempt to reconnect...', false);
    scheduleReconnect();
  };

  try {
    stompClient.connect({}, onConnect, onError);
    // SockJS close event
    stompSock.onclose = () => {
      console.warn('SockJS closed');
      stompConnected = false;
      appendChat('Signaling socket closed', false);
      scheduleReconnect();
    };
  } catch (e) {
    console.error('connect failed', e);
    scheduleReconnect();
  }
}

function scheduleReconnect() {
  if (reconnectAttempts >= maxReconnectAttempts) {
    appendChat('Max reconnect attempts reached. Please reload the page.', false);
    return;
  }
  reconnectAttempts++;
  const delay = Math.min(30000, baseReconnectDelay * Math.pow(2, reconnectAttempts));
  appendChat('Reconnecting in ' + (delay/1000).toFixed(1) + 's (attempt ' + reconnectAttempts + ')', false);
  setTimeout(() => {
    try {
      // close previous if any
      if (stompClient) {
        try { stompClient.disconnect(); } catch(_) {}
        stompClient = null;
      }
      connect();
    } catch (e) { console.error('reconnect attempt failed', e); scheduleReconnect(); }
  }, delay);
}

// function subscribeRoom() {
//   if (!stompClient || !roomId) return;
//   // chat topic
//   stompClient.subscribe('/topic/chat.' + roomId, frame => {
//     const msg = JSON.parse(frame.body);
//     appendChat(msg.sender + ': ' + msg.content, msg.sender === username);
//   });
//   // signaling topic
//   stompClient.subscribe('/topic/signal.' + roomId, frame => {
//     try {
//       const signal = JSON.parse(frame.body);
//       if (signal.fromUser === username) return;
//       if (signal.kind === 'offer') { isInitiator = false; handleOffer(signal); }
//       else if (signal.kind === 'answer') handleAnswer(signal);
//       else if (signal.kind === 'candidate') handleCandidate(signal);
//       else if (signal.kind === 'hangup') doHangup();
//     } catch (e) { console.error('signal parse error', e); }
//   });
// }

function subscribeRoom() {
  if (!stompClient || !roomId) return;

  // unsubscribe old
  if (chatSub) chatSub.unsubscribe();
  if (signalSub) signalSub.unsubscribe();

  // subscribe to new
  chatSub = stompClient.subscribe('/topic/chat.' + roomId, frame => {
    const msg = JSON.parse(frame.body);
    appendChat(msg.sender + ': ' + msg.content, msg.sender === username);
  });

  signalSub = stompClient.subscribe('/topic/signal.' + roomId, frame => {
    const signal = JSON.parse(frame.body);
    if (signal.fromUser === username) return;
    if (signal.kind === 'offer') { isInitiator = false; handleOffer(signal); }
    else if (signal.kind === 'answer') handleAnswer(signal);
    else if (signal.kind === 'candidate') handleCandidate(signal);
    else if (signal.kind === 'hangup') doHangup();
  });
}


function sendChat(text) {
  if (!stompClient || !roomId) { appendChat('Not connected to server; message not sent', false); return; }
  const msg = { sender: username, receiver: peername || '', roomId: roomId, content: text, type: 'CHAT' };
  try { stompClient.send('/app/chat.send/' + roomId, {}, JSON.stringify(msg)); }
  catch (e) { console.error('sendChat failed', e); appendChat('Failed to send message', false); }
}

if (sendBtn) {
  // sendBtn.onclick = () => {
  //   const t = chatInput.value.trim(); if (!t) return;
  //   sendChat(t); appendChat((username || 'me') + ': ' + t, true); chatInput.value = '';
  // };
  sendBtn.onclick = () => {
  const t = chatInput.value.trim();
  if (!t) return;
  sendChat(t);
  chatInput.value = '';
};

}

if (joinBtn) {
  joinBtn.onclick = () => {
    username = (usernameIn && usernameIn.value ? usernameIn.value.trim() : username || ('anon-' + Math.floor(Math.random()*10000)));
    peername = (peernameIn && peernameIn.value ? peernameIn.value.trim() : '');
    if (!username) { alert('enter your name'); return; }
    if (peername) {
      roomId = (username < peername) ? username + '_' + peername : peername + '_' + username;
    }
    connect();
    if (joinBtn) joinBtn.disabled = true; if (leaveBtn) leaveBtn.disabled = false;
    appendChat('Joined as ' + username + (peername ? ' (room: ' + roomId + ')' : ''), false);
    if (roomId) subscribeRoom();
  };
}

if (leaveBtn) {
  leaveBtn.onclick = () => {
    if (stompClient) stompClient.disconnect(() => console.log('disconnected'));
    stompClient = null; roomId = null; if (joinBtn) joinBtn.disabled = false; if (leaveBtn) leaveBtn.disabled = true;
  };
}

// Matchmaking
if (findRandomBtn) findRandomBtn.onclick = () => {
  console.log('Find Random Match button clicked');
  appendChat('Searching...', false);
  
  // allow matchmaking without explicit Join: auto-connect and provide a default username
  // if (!username) {
  //   username = '-' + Math.floor(Math.random()*10000);
  //   if (usernameIn) usernameIn.value = username;
  //   console.log('Generated username:', username);
  // }
  if (!username || username.startsWith('-')) {
  const inputName = usernameIn && usernameIn.value.trim();
  username = inputName || username || ('user-' + Math.floor(Math.random() * 10000));
  if (usernameIn) usernameIn.value = username;
  console.log('Using username:', username);
}


  const doJoin = () => {
    try {
      console.log('Sending match join request...');
      stompClient.send('/app/match/join', {}, JSON.stringify({ clientId: clientId, username: username, mode: 'video' }));
      // appendChat('Searching...', false);
    } catch (e) { 
      console.error('match join failed', e);
      appendChat('Failed to join matchmaking', false);
    }
  };

  if (!stompConnected) {
    console.log('Not connected, connecting first...');
    // connect then wait for STOMP connection
    if (!stompClient) connect();
    waitForStompConnected(10000).then(() => {
      console.log('Connected, joining match...');
      doJoin();
    }).catch(() => {
      console.error('Connection timeout');
      appendChat('Could not connect to signaling server', false);
    });
  } else {
    console.log('Already connected, joining match...');
    doJoin();
  }
};

// allow manual start call via button
if (startCallBtn) {
  startCallBtn.onclick = () => {
    if (!roomId) { appendChat('No room available. Use Next to find a match first.', false); return; }
    startCall();
  };
}

// helper: wait for stompConnected flag with timeout
function waitForStompConnected(timeoutMs = 5000) {
  return new Promise((resolve, reject) => {
    const interval = 100;
    let waited = 0;
    const t = setInterval(() => {
      if (stompConnected) { clearInterval(t); resolve(); }
      waited += interval;
      if (waited >= timeoutMs) { clearInterval(t); reject(new Error('timeout')); }
    }, interval);
  });
}

// WebRTC helpers
async function startCall() {
  try {
  pc = new RTCPeerConnection(iceConfig);
    localStream = await navigator.mediaDevices.getUserMedia({ audio: true, video: true });
    localVideo.srcObject = localStream; localStream.getTracks().forEach(t => pc.addTrack(t, localStream));
  pc.ontrack = e => { const rv = ensureRemoteVideo(); rv.srcObject = e.streams[0]; };
  pc.onicecandidate = e => { if (e.candidate) sendSignal('candidate', JSON.stringify(e.candidate)); };
  pc.onconnectionstatechange = () => { handlePcState(pc.connectionState); };
  pc.oniceconnectionstatechange = () => { handleIceState(pc.iceConnectionState); };
  isInitiator = true;
  const offer = await pc.createOffer(); await pc.setLocalDescription(offer); sendSignal('offer', JSON.stringify(offer));
  } catch (err) { console.error(err); alert('Could not start call: ' + err.message); }
}

function sendSignal(kind, payload) {
  if (!stompClient || !roomId) return;
  stompClient.send('/app/signal/' + roomId, {}, JSON.stringify({ roomId: roomId, fromUser: username, kind: kind, payload: payload }));
}

async function handleOffer(signal) {
  try {
    pc = new RTCPeerConnection(iceConfig);
    localStream = await navigator.mediaDevices.getUserMedia({ audio: true, video: true });
    localVideo.srcObject = localStream; localStream.getTracks().forEach(t => pc.addTrack(t, localStream));
    pc.ontrack = e => { const rv = ensureRemoteVideo(); rv.srcObject = e.streams[0]; };
    pc.onicecandidate = e => { if (e.candidate) sendSignal('candidate', JSON.stringify(e.candidate)); };
    pc.onconnectionstatechange = () => { handlePcState(pc.connectionState); };
    pc.oniceconnectionstatechange = () => { handleIceState(pc.iceConnectionState); };
    isInitiator = false;
    await pc.setRemoteDescription(JSON.parse(signal.payload));
    const answer = await pc.createAnswer(); await pc.setLocalDescription(answer); sendSignal('answer', JSON.stringify(answer));
  } catch (e) { console.error('handleOffer error', e); appendChat('Error handling offer: ' + e.message, false); }
}

async function handleAnswer(signal) { if (pc) await pc.setRemoteDescription(JSON.parse(signal.payload)); }
async function handleCandidate(signal) { if (pc) await pc.addIceCandidate(JSON.parse(signal.payload)); }

// function doHangup() {
//   if (pc) { pc.close(); pc = null; }
//   if (localStream) { localStream.getTracks().forEach(t => t.stop()); localStream = null; }
//   if (remoteVideo) remoteVideo.srcObject = null;
// }
function doHangup() {
  if (pc) { pc.close(); pc = null; }
  if (localStream) { localStream.getTracks().forEach(t => t.stop()); localStream = null; }
  if (remoteVideo) remoteVideo.srcObject = null;
  // unsubscribe old room to prevent ghost messages
  if (chatSub) { chatSub.unsubscribe(); chatSub = null; }
  if (signalSub) { signalSub.unsubscribe(); signalSub = null; }
  roomId = null;
}

if (hangupBtn) hangupBtn.onclick = () => { sendSignal('hangup', ''); doHangup(); };

// safe sendSignal wrapper
function sendSignal(kind, payload) {
  if (!stompClient || !roomId) { appendChat('Signaling not connected; cannot send ' + kind, false); return; }
  try { stompClient.send('/app/signal/' + roomId, {}, JSON.stringify({ roomId: roomId, fromUser: username, kind: kind, payload: payload })); }
  catch (e) { console.error('sendSignal failed', e); appendChat('Failed to send signal: ' + kind, false); }
}

// WebRTC state handling
function handlePcState(state) {
  console.log('PC state', state);
  // appendChat('Connection state: ' + state, false);
  if (state === 'failed' || state === 'disconnected') {
    // try ICE restart if initiator
    if (pc && isInitiator) {
      try {
        if (pc.restartIce) { pc.restartIce(); appendChat('Requested ICE restart', false); }
        else {
          // create new offer with iceRestart
          pc.createOffer({ iceRestart: true }).then(o => pc.setLocalDescription(o)).then(() => sendSignal('offer', JSON.stringify(pc.localDescription))).catch(e => console.error('re-offer failed', e));
        }
      } catch (e) { console.error('ice restart failed', e); }
    }
  }
}

function handleIceState(state) {
  console.log('ICE state', state);
  // appendChat('ICE connection state: ' + state, false);
  if (state === 'failed') {
    // attempt restart similar to above
    if (pc && isInitiator) {
      try { pc.createOffer({ iceRestart: true }).then(o => pc.setLocalDescription(o)).then(() => sendSignal('offer', JSON.stringify(pc.localDescription))).catch(e => console.error('re-offer failed', e)); }
      catch (e) { console.error('ice restart error', e); }
    }
  }
}

// expose for debugging
window._debug = { clientId };
