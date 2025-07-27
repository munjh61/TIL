import useInput from './../hooks/useInput'

const HookExam = () => {
    const [input, onChange] = useInput()
    return (
        <input value={input} onChange={onChange}/>
    )
}

export default HookExam