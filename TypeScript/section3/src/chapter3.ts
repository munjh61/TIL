// 객체 타입간의 호환성

type Animal = {
    name: string
    color: string
}

type Dog = {
    name: string
    color : string
    breed: string
}

let animal:Animal = {
    name:'기린',
    color:'yellow',
}

let dog:Dog = {
    name:'돌돌이',
    color:'brown',
    breed:'진도',
}

// Dog 타입이 Animal 타입의 모든 속성을 가지고 있어서, Dog 타입이 서브 타입이 된다.

animal = dog; // 가능. 업 캐스팅
// dog = animal // 불가능. 다운 캐스팅

type Book = {
    name : string
    price : number
}


type ProgrammingBook = {
    name : string
    price : number
    skill: string
}

let book1 : Book

let programmingBook : ProgrammingBook = {
    name : '타입스크립트',
    price : 33000,
    skill : 'type script'
}

// 업 캐스팅으로 없는 속성이 있어도 넣을 수 있다.
book1 = programmingBook

// 하지만 생성할 때는 초과 프로퍼티 검사 때문에 만들 수 없다.
let book2 : Book = {
    name : '타입스크립트',
    price : 33000,
    // skill : 'type script', // 초과 프로퍼티 검사에서 걸림
}

let book3 : Book = programmingBook // 이 경우는 초과 프로퍼티 검사에 안 걸림

// 함수에서도 마찬가지
// 인수에 Book을 넣는 함수가 있고
function func(book : Book){}

func({
    name : '타입스크립트',
    price : 33000,
    // skill : 'type script' // 이 경우 초과프로퍼티 검사에서 걸리지만
})

func(programmingBook) // 이미 만들어진 서브타입 변수를 넣는 것은 가능하다.