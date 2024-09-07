let isRecording = false;

function scrollToConverter() {
    document.getElementById('converter').scrollIntoView({ behavior: 'smooth' });
}

function toggleRecording() {
    isRecording = !isRecording;
    const recordBtn = document.getElementById('recordBtn');
    if (isRecording) {
        recordBtn.textContent = 'Stop Recording';
        recordBtn.style.backgroundColor = '#ff0000';
        simulateRecording();
    } else {
        recordBtn.textContent = 'Start Recording';
        recordBtn.style.backgroundColor = '#00ffff';
    }
}

function simulateRecording() {
    if (isRecording) {
        const inputText = document.getElementById('inputText');
        inputText.value += ' ' + generateRandomWord();
        setTimeout(simulateRecording, 500);
    }
}

function generateRandomWord() {
    const words = ['hello', 'world', 'sign', 'language', 'converter', 'audio', 'text', 'technology', 'future', 'communication'];
    return words[Math.floor(Math.random() * words.length)];
}

function convertToISL() {
    const inputText = document.getElementById('inputText').value;
    const outputVideo = document.getElementById('outputVideo');
    outputVideo.innerHTML = 'Converting: "' + inputText + '" to ISL...';
    // Simulate conversion delay
    setTimeout(() => {
        outputVideo.innerHTML = 'ISL animation for: "' + inputText + '"';
    }, 2000);
}

function toggleMenu() {
    const nav = document.getElementById('mainNav');
    nav.classList.toggle('active');
}