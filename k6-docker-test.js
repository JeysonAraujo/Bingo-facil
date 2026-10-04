import ws from 'k6/ws';
import { check } from 'k6';
import { sleep } from 'k6';

export const options = {
    vus: 100,
    duration: '30s',
};

const WS_URL = 'ws://host.docker.internal:8080/ws/bingo/websocket';

export default function () {
    const res = ws.connect(WS_URL, null, function (socket) {
        socket.on('open', () => {
            // Send STOMP CONNECT frame
            socket.send('CONNECT\naccept-version:1.1,1.0\nheart-beat:10000,10000\n\n\0');
        });

        socket.on('message', (msg) => {
            if (msg.includes('CONNECTED')) {
                check(msg, { 'connected successfully': (m) => m.includes('CONNECTED') });
                
                // Subscribe to a generic event topic (simulating a player)
                socket.send(`SUBSCRIBE\nid:sub-0\ndestination:/topic/events/mock-event-id/draws\n\n\0`);
            }
        });

        socket.on('close', () => {
            // connection closed
        });

        socket.setTimeout(function () {
            socket.close();
        }, 25000);
    });

    check(res, { 'status is 101': (r) => r && r.status === 101 });
}
