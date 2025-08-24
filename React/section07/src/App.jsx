import './App.css'
import Viewer from './components/Viewer'
import Controller from './components/Controller'
import Even from './components/Even'
import { useState, useEffect } from 'react'
function App() {
  const [count, setCount] = useState(0)

  // count가 바뀔 때마다 함수가 발동함
  useEffect(()=>{}, [count])

  const onClickButton = (value) =>{
    setCount(count + value);
  }

  return (
    <>
    <h1>Simple Counter</h1>
      <section>
        <Viewer count={count}/>
        {count % 2 ===0 ? <Even/> : null}
      </section>
      <section>
        <Controller onClickButton={onClickButton}/>
      </section>
    </>
  )
}

export default App
