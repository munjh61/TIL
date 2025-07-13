// number
let num1: number = 123
let num2: number = -123
let num3: number = 0.123
let num4: number = -0.123
let num5: number = Infinity
let num6: number = -Infinity
let num7: number = NaN

// number가 아닌 타입은 불가하게 됨
// num1 = 'hello'
// num1.toUpperCase() 
num1.toFixed() // number에게 사용가능한 함수만 가능

let str1: string = 'hello'
let str2: string = "hello"
let str3: string = `hello`
let str4: string = `hello ${num1}`

// string가 아닌 타입 불가하게 됨
// str1 = 123
// str1.toFixed()

str1.toUpperCase() // string에게 사용가능한 함수만 가능

// boolean
let bool1: boolean = true
let bool2: boolean = false

// null
let null1: null = null

// undefined
let unde1: undefined = undefined

// 임시로 null을 넣어야할 경우가 생긴다. 이럴 땐 strictNullChecks를 false로 처리한다.
// let numA:number = null; 

// 리터럴 타입
let numA: 10 = 10;
// numA = 12 안됨
let strA: 'hello' = 'hello'
// strA = 'bye' 안됨
let boolA: true = true
// boolA = false 안됨