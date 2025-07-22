import Button from "./components/Button";

function App() {
  const buttonProps = {
    text: "카페",
    color: "green",
    a: 1,
    b: 2,
    c: 3,
  };

  return (
    <>
      <Button text={"메일"} color={"red"} />
      <Button {...buttonProps} /> {/* 구조분해할당 */}
      <Button text={"블로그"}>
        <div>자식 요소</div>
      </Button>
    </>
  );
}

export default App;
