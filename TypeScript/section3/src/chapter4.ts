// 대수 타입
// 여러 개의 타입을 합성해서 새롭게 만들어낸 타입

// 1. 합집합
let a: string | number | boolean

a = 1
a = 'hello'
a = true

let arr : (number | string | boolean)[] = [1, 'hello', true]

type Dog = {
    name : string,
    color : string,
}

type Person = {
    name : string,
    language : string,
}

type Union1 = Dog | Person

let union1 : Union1 = {
    name : '',
    color : '',
}

let union2 : Union1 = {
    name : '',
    language : ''
}

let union3 : Union1 = {
    name : '',
    color : '',
    language : ''
}

// 두 타입 중 하나 이상 만족해야함
// let union4 : Union1 = {
//     name : '',
// }

// 2. 교집합

let variable : number & string // = never

type Intersection = Dog & Person

// 모두를 만족시켜야하기 때문에 모든 속성을 가지고 있어야 함
let intersection1 : Intersection = {
    name : '',
    color : '',
    language : '',
}