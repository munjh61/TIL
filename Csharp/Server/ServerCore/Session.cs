using System.Net.Sockets;
using System.Text;

namespace ServerCore
{
    class Session
    {
        Socket _socket;
        int _disconnected = 0;
        object _lock = new object();
        Queue<byte[]> _sendQueue = new Queue<byte[]>(); // 현재 Send 함수 인수가 byte[]라 형식이 byte[]
        bool _pending = false;
        SocketAsyncEventArgs _sendArgs = new SocketAsyncEventArgs(); // 내부에 만들어도 재사용이 가능했던 Recv와 다르게 Send는 재사용을 위해선 Send 함수 밖에서 만들어야함
        public void Start(Socket socket)
        {
            _socket = socket;
            SocketAsyncEventArgs recvArgs = new SocketAsyncEventArgs();
            recvArgs.Completed += new EventHandler<SocketAsyncEventArgs>(onRecvCompleted);
            //recvArgs.UserToken = this; // 온갖 정보를 넣어줄 수 있음
            recvArgs.SetBuffer(new byte[1024], 0, 1024);
            _sendArgs.Completed += new EventHandler<SocketAsyncEventArgs>(OnSendCompleted);

            RegisterRecv(recvArgs);
        }
        // 송신
        public void Send(byte[] sendBuff) // 지금은 강의 단계에선 바이트지만 나중엔 package로 보냄.
        {
            lock (_lock)
            {
                _sendQueue.Enqueue(sendBuff);
                if (_pending == false)
                    RegisterSend();
            }

        }
        public void Disconnect()
        {
            if (Interlocked.Exchange(ref _disconnected, 1) == 1)
                return;
            _socket.Shutdown(SocketShutdown.Both);
            _socket.Close();
        }

        #region 네트워크 통신
        void RegisterSend()
        {
            _pending = true;
            byte[] buff = _sendQueue.Dequeue();
            _sendArgs.SetBuffer(buff, 0, buff.Length);

            bool pending = _socket.SendAsync(_sendArgs);
            if (pending == false)
                OnSendCompleted(null, _sendArgs);
        }
        void OnSendCompleted(object sender, SocketAsyncEventArgs args)
        {
            lock (_lock)
            {
                if (args.BytesTransferred > 0 && args.SocketError == SocketError.Success)
                {
                    try
                    {
                        if (_sendQueue.Count > 0) // 내가 하는 동안 누군가가 Send를 했을 때 그걸 처리해주기
                            RegisterSend();
                        else
                            _pending = false;
                    }
                    catch (Exception ex)
                    {
                        Console.WriteLine(ex.ToString());
                    }
                }
                else
                {
                    Disconnect();
                }
            }
        }

        void RegisterRecv(SocketAsyncEventArgs args)
        {
            bool pending = _socket.ReceiveAsync(args);
            if (pending == false)
                onRecvCompleted(null, args);
        }
        void onRecvCompleted(object sender, SocketAsyncEventArgs args)
        {
            if (args.BytesTransferred > 0 && args.SocketError == SocketError.Success)
            {
                //TODO
                try
                {
                    string recvData = Encoding.UTF8.GetString(args.Buffer, args.Offset, args.BytesTransferred);
                    Console.WriteLine($"[From Client] {recvData}");
                    RegisterRecv(args);
                }
                catch (Exception e)
                {
                    Console.WriteLine($"OnRecvCompleted Failed {e}");
                }
            }
            else
            {
                //Todo Disconnect
                Disconnect();
            }
        }
        #endregion
    }
}
