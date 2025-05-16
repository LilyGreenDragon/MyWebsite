function deleteImage() {
var dropBox;
var base64src;

dropBox = document.getElementById("dropBox");
dropBox.style.backgroundImage = "none";
base64src = "";
document.getElementById("hiddenField2").setAttribute("value", base64src);
//dropBox.innerHTML = '<div id="text">Тащи сюда уже че нибудь...</div>'
}

