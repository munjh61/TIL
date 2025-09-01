using System.Net;
using System.Net.Sockets;
using System.Text;

namespace serverCore
{
    class Program
    {
        static void Main(string[] args)
        {
            // DNS
            string host = Dns.GetHostName();
            IPHostEntry ipHost = Dns.GetHostEntry(host);
            IPAddress ipAddr = ipHost.AddressList[0]; // 많이 몰리는 사이트에선 IP여러개를 주기도 함
            IPEndPoint endPoint = new IPEndPoint(ipAddr, 7777); // 클라이언트가 접속할 주소랑 맞추기
            while (true)
            {
                try
                {
                    // 고객
                    Socket socket = new Socket(endPoint.AddressFamily, SocketType.Stream, ProtocolType.Tcp);
                    // 문지기에게 입장 문의
                    socket.Connect(endPoint);
                    Console.WriteLine($"Connected To {socket.RemoteEndPoint}");
                    // 보낸다
                    for (int i = 0; i < 5; i++)
                    {
                        byte[] sendBuff = Encoding.UTF8.GetBytes($"Hello World! {i}");
                        int sendBytes = socket.Send(sendBuff);
                    }
                    // 받는다
                    byte[] recvBuff = new byte[1024];
                    int recvBytes = socket.Receive(recvBuff);
                    string recvData = Encoding.UTF8.GetString(recvBuff);
                    Console.WriteLine($"[From Server] {recvData}");
                    // 나간다
                    socket.Shutdown(SocketShutdown.Both);
                    socket.Close();
                }
                catch (Exception e)
                {
                    Console.WriteLine(e.ToString());
                }
                Thread.Sleep(100);
            }
        }
    }
}