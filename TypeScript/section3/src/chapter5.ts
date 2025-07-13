// 타입 추론

// 1. 초기값으로 타입 추론
let a = 10
let b = 'hello'
let c = {
    id : 1,
    name : '문땡땡',
    profile : {
        nickname : 'moon'
    }
}
let {id, name, profile} = c
let [one, two, three] = [1, 'hello', true]
let arr = [1, "string"]

function func(message = 'hello'){ // 파라미터의 기본 값으로 타입 추론
    console.log(message)
    return 'hello' // 반환 값으로 타입 추론
}

let d; // 처음 선언시 초기값을 선언하지 않다면 any타입이 된다.
// 값을 넣을 경우 사용할 수 있는 속성만 사용 가능하다.
d = 10 
d.toFixed
// d.toUpperCase()

// 이런 경우 string으로 재할당이 가능하다!!
d = 'hello'
// d.toFixed
d.toUpperCase()

// const로 선언을 했다면 리터럴 타입이 된다
const num = 10
const str = 'hello'