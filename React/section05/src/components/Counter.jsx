import { useState } from "react";

const Counter = () => {
    // state : 값 , setState : 값을 바꾸는 함수
    const [state, setState] = useState(0);

    return (
        <div>
            <h1>{state}</h1>
            <button onClick={() => {
                setState(state + 1)
            }}>버튼</button>
        </div>
    )
}

export default Counter