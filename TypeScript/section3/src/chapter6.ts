// 타입 단언

type Person = {
    name: string
    age: number
}

// 미리 변수와 타입을 선언 해놓고 나중에 값을 설정하고 싶다면 어떻게 할까?

// 이 두 방법 다 안된다.

// 1. 빈객체로 만들어보기
// let person : Person = {} // 속성이 없으면 못 만듬

// 2. 객체에 타입 설정안하고 속성 넣어보기
// let person;
// person.name = '문땡땡' // 해당 속성이 없으면 못 만듬
// person.age = 29 // 해당 속성이 없으면 못 만듬

let person = {} as Person
person.name = '문땡땡'
person.age = 29

type Dog = {
    name: string,
    color: string
}

// breed는 Dog 속성에 없다. 하지만 as를 이용한다면 사용 가능하다.
let dog = {
    name: '돌돌이',
    color: 'brown',
    breed: '진도'
} as Dog

// 타입 단언의 규칙
// 값 as 단언 <- 단언식
// A as B에서 A가 B의 슈퍼타입이거나 서브 타입이어야한다.
let num1 = 10 as never // 10이 never의 슈퍼 타입
let num2 = 10 as unknown // 10이 unknown의 서브 타입
// let num3 = 10 as string //'number' 형식을 'string' 형식으로 변환한 작업은 실수일 수 있습니다. 두 형식이 서로 충분히 겹치지 않기 때문입니다. 의도적으로 변환한 경우에는 먼저 'unknown'으로 식을 변환합니다.

// const 단언

let cat = {
    name : '야옹이',
    color: 'yello',
} as const

// cat.name = '' // 읽기 전용으로 name을 할당할 수 없게 됨

// Not Null 선언. 느낌표

type Post = {
    title : string
    author? : string
}

let post : Post = {
    title : '게시글1',
    author : '문땡땡',
}

// const len : number = post.author?.length // 'number | undefined' 형식은 'number' 형식에 할당할 수 없습니다.
const len : number = post.author!.length
// 느낌표를 붙이면 이 값이 null, undefined 값이 올 수 없다고 compiler를 믿게 만듬