import "./Main.css";

const Main = () => {
  const user = {
    name: "문준호",
    isLogin: true,
  };

  return (
    <>
      {user.isLogin ? (
        <div className="logout">로그아웃</div>
      ) : (
        <div>로그인</div>
      )}
    </>
  );
};

export default Main;
