export const hello = () => {
    console.log('hello')
}

// compileOptions의 strict 옵션이 true라면 message의 타입을 정해주어야한다.
export const hello2 = (message) => {
    console.log('hello' + message)
}