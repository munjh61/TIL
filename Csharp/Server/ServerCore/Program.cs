using ServerCore;
using System;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;

namespace serverCore
{
    class Program
    {
        static Listener _listener = new Listener();
        static void OnAcceptHandler(Socket clientSocket) // 클라이언트 접속 시 실행되는 콜백
        {
            try
            {
                Session session = new Session();
                session.Start(clientSocket);

                byte[] sendBuff = Encoding.UTF8.GetBytes("Welcome to Junho Server");
                session.Send(sendBuff);
                Thread.Sleep(1000);
                session.Disconnect();
            }
            catch (Exception e)
            {
                Console.WriteLine(e.ToString());
            }
        }
        static void Main(string[] args)
        {
            // DNS. 서버에서 사용할 IP와 Port 설정
            string host = Dns.GetHostName();
            IPHostEntry ipHost = Dns.GetHostEntry(host);
            IPAddress ipAddr = ipHost.AddressList[0]; // 많이 몰리는 사이트에선 IP여러개를 주기도 함
            IPEndPoint endPoint = new IPEndPoint(ipAddr, 7777); // 포트 7777로 대기. 클라이언트가 접속할 주소랑 맞추기

            _listener.Init(endPoint, OnAcceptHandler);
            Console.WriteLine("Listening...");
            // 메인 스레드 유지
            while (true)
            {

            }
        }
    }
}