import { useState } from "react";

const Register = () => {
    const [name, setName] = useState('초기값');
    const [birth, setBirth] = useState('');
    const [country, setCountry] = useState('')
    const [bio, setbio] = useState('')

    const onChangeName = (e) => {
        // console.log(e.target.value);
        setName(e.target.value);
    }
    const onChangeBirth = (e) => {
        setBirth(e.target.value)
    }
    const onChangeCountry = (e) =>{
        setCountry(e.target.value);
    }
    const onChangeBio = (e) =>{
        setCountry(e.target.value);
    }
    return (
        <div>
            <div>
                <input value={name} onChange={onChangeName} placeholder="이름" />
            </div>
            <div>
                <input value={birth} type="date" onChange={onChangeBirth}/>
            </div>
            <div>
                <select value={country} onChange={onChangeCountry}>
                    <option value=""></option>
                    <option value='kr'>한국</option>
                    <option value='us'>미국</option>
                    <option value="uk">영국</option>
                </select>
            </div>
            <h1>자기소개</h1>
            <div>
                <textarea value={bio} onChange={onChangeBio}></textarea>
            </div>
        </div>
    )
}

export default Register