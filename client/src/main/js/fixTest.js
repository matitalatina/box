
if(!document.queryCommandSupported) {
    console.log('Fix queryCommandSupported')
    document.queryCommandSupported = () => false
}

function noOp () { }
if (typeof window.URL.createObjectURL === 'undefined') {
    Object.defineProperty(window.URL, 'createObjectURL', { value: noOp})
}