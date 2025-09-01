using System.Net;
using System.Net.Sockets;

namespace ServerCore
{
    class Listener
    {
        Socket _listenSocket; // 서버 리스닝 소켓
        Action<Socket> _onAcceptHandler; // 클라이언트 접속 시 실행할 콜백
        public void Init(IPEndPoint endPoint, Action<Socket> onAcceptHandler)
        {
            // 문지기 교육
            _listenSocket = new Socket(endPoint.AddressFamily, SocketType.Stream, ProtocolType.Tcp);
            _onAcceptHandler += onAcceptHandler; // 콜백 등록
            _listenSocket.Bind(endPoint);   // 지정된 IP:PORT에 바인딩하여 클라이언트 요청을 받을 준비
            _listenSocket.Listen(10);       // backlog: 최대 대기수

            SocketAsyncEventArgs args = new SocketAsyncEventArgs(); // AcceptAsync 호출에 필요한 이벤트 인자 객체
            args.Completed += new EventHandler<SocketAsyncEventArgs>(OnAcceptCompleted); // .Completed → AcceptAsync, ReceiveAsync, SendAsync 같은 비동기 소켓 작업이 끝났을 때 호출되는 이벤트
            RegisterAccept(args); // 첫 Accept 요청 등록
        }
        void RegisterAccept(SocketAsyncEventArgs args)
        {
            args.AcceptSocket = null; // 이전 소켓 정보 초기화
            // AcceptAsync는 true 반환 시 비동기로 대기 진행, false 반환 시 즉시 완료된 상태.
            // .Completed 는 비동기로 대기 중이었던 작업이 완료되었을 때만 발동한다.
            bool pending = _listenSocket.AcceptAsync(args);
            if (pending == false) // 즉시 연결이 완료되었다면 수동으로 콜백 호출
                OnAcceptCompleted(null, args);
        }
        void OnAcceptCompleted(object sender, SocketAsyncEventArgs args)
        {
            if(args.SocketError == SocketError.Success)
            {
                //TODO
                _onAcceptHandler.Invoke(args.AcceptSocket); // 연결된 소켓 전달, 이제 얘 처리하자
            }
            else
                Console.WriteLine(args.SocketError.ToString());
            RegisterAccept(args); // 다음 클라이언트 접속 대기
        }

    }
}
