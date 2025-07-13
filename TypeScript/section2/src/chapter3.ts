// object
let user1: object = {
    id: 1,
    name: '문땡땡'
}

// user를 object로 설정해놓을 경우
// object에 id라는 속성이 없으므로
// user1.id; 는 사용 할 수 없다.

// 객체 리터럴 타입을 사용해야 점 표기법을 사용할 수 있다.
let user2: {
    id?: number,
    name: string
} = {
    id: 1,
    name: '문땡땡'
}

user2.id; // 가능

user2 = {
    name: '홍길동'
}

//read only
let config:{
    readonly apiKey: string
} = {
    apiKey : 'MY API KEY'
}

// config.apiKey = 'Changed API KEY'