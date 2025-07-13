// 타입 별칭
type User = {
    id : number,
    name : string
    nickname : string,
    birth:string,
    location: string;
}

let user: User = {
    id : 1,
    name : '문땡땡',
    nickname : 'moon',
    birth : '1996.06.01',
    location : '강남구'
}

// 인덱스 시그니처
type CountryCodes = {
    [key : string] : string;
}
let countryCodes : CountryCodes = {
    Korea : 'ko',
    UnitedState : 'us',
    UnitedKingdom : 'uk',
}

type CountryNumberCodes = {
    [key: string] : number;
    Korea: number // 인덱스 시그니처의 타입와 일치 또는 호환되어야함. string 안됨
}

let countryNumberCodes : CountryNumberCodes = {
    Korea : 410, // [] 이외에 적힌 것은 필수
    UnitedState : 840,
    UnitedKingdom : 826
}