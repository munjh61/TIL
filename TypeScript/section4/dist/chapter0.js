function func(a, b) {
    return a + b;
}
const add = (a, b) => a + b;
function introduce(name = '문준호', tall) {
    console.log(`name : ${name}`);
    if (typeof tall === 'number')
        console.log(`tall : ${tall + 10}`);
}
introduce('문준호');
introduce('문준호', 173);
function getSum(...rest) {
    let sum = 0;
    rest.forEach((it) => sum += it);
    return sum;
}
export {};
