const Button = (children, text, color = "black") => {
  return (
    <button
      onClick={() => {
        console.log(text);
      }}
      style={{ color: color }}
    >
      {text}
      {children}
    </button>
  );
};

export default Button;

// const Button = (props) => {
//   console.log(props);
//   return <button style={{ color: props.color }}>{props.text}</button>;
// };

// 19 버전 이후로 defaultProps는 없어짐
// Button.defaultProps = {
//     color : 'black'
// }

// props에 기본값 설정
