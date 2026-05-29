// Type declaration for the SockJS minified bundle path used in websocket.ts.
// The @types/sockjs-client package only covers the default export path, not
// the explicit /dist/sockjs.min.js sub-path. This declaration bridges the gap.
declare module 'sockjs-client/dist/sockjs.min.js' {
  import SockJS from 'sockjs-client'
  export = SockJS
}
