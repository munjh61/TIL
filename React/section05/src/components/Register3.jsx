import { useState, useRef } from "react";

const Register = () => {
    const [input, setInput] = useState({
        name: '',
        birth: '',
        country: '',
        bio: '',
    })
    
    const onChange = (e) => {
        setInput({
            ...input,
            [e.target.name]: e.target.value
        })
    }

    // const refObj = useRef(0); // 초기값을 소괄호 안에 넣음
    const inputRef = useRef();

    const onSubmit = () => {
        if(input.name === ''){
            inputRef.current.focus(); // 이름을 입력하지 않았다면 포커싱해라
        }
    }

    return (
        <div>
            {/* <button onClick={()=>{
                refObj.current++
                console.log(refObj.current)
            }}>useRef버튼</button> */}

            <div>
                <input ref={inputRef} name="name" value={input.name} onChange={onChange} placeholder="이름" />
            </div>
            <div>
                <input name="birth" value={input.birth} type="date" onChange={onChange} />
            </div>
            <div>
                <select name="country" value={input.country} onChange={onChange}>
                    <option value=""></option>
                    <option value='kr'>한국</option>
                    <option value='us'>미국</option>
                    <option value="uk">영국</option>
                </select>
            </div>
            <h1>자기소개</h1>
            <div>
                <textarea name="bio" value={input.bio} onChange={onChange}></textarea>
            </div>
            <button onClick={onSubmit}>제출</button>
        </div>
    )
}

export default Register