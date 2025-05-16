var dropBox;
var base64src;

window.onload = function() {
dropBox = document.getElementById("cropperContainer");
dropBox.ondragenter = ignoreDrag;
dropBox.ondragover = ignoreDrag;
dropBox.ondrop = drop;

}

function ignoreDrag(e) {
  // Обеспечиваем, чтобы никто другой не получил это событие,
  // т.к. мы выполняем операцию перетаскивания
  e.stopPropagation();
  e.preventDefault();
}

function drop(e) {
  // Аннулируем это событие для всех других
  e.stopPropagation();
  e.preventDefault();

  // Получаем перемещенные файлы
  var data = e.dataTransfer;
  var files = data.files;

  // Передаем полученный файл функции для обработки файлов
  processFiles(files);
}


function processFiles(files) {
 //let imgInput = document.getElementById('fileInput');
      //  imgInput.addEventListener('change', function (e) {
           // if (e.target.files) {
              //  let imageFile = e.target.files[0];
              var imageFile = files[0];

                if (!/^image/.test(imageFile.type)) {
                     //alert('Выбранный файл не является изображением!');
                     swal("Сообщение!", "Выбранный файл не является изображением!");
                     return;
                   }


               // if(imageFile.size>1024||imageFile.size<15){
                var reader = new FileReader();
                reader.onload = function (e) {
                    var img = document.createElement("img");
                    img.onload = function (event) {
                    var MAX_WIDTH = 1000;
                    var MAX_HEIGHT = 1000;

                    var MIN_WIDTH = 150;
                    var MIN_HEIGHT = 150;

                    var width = img.width;
                    var height = img.height;

                     if (width/height>4||height/width>4) {
                     //alert('Что за длинная сосиска! Давай что-нибудь покороче!');
                     swal("Сообщение!", "Что за длинная сосиска! Давайте что-нибудь покороче!");
                     return;
                   }

                    if(width<126 || height<126){

                                            if (width < MIN_WIDTH) {
                                                height = height * (MIN_WIDTH / width);
                                                width = MIN_WIDTH;
                                            }
                                         if (height < MIN_WIDTH){
                                            //height < MIN_HEIGHT)
                                                width = width * (MIN_HEIGHT / height);
                                                height = MIN_HEIGHT;

                                        }
                    }else{

                    // Change the resizing logic
                    if (width > height) {
                        if (width > MAX_WIDTH) {
                            height = height * (MAX_WIDTH / width);
                            width = MAX_WIDTH;
                        }
                    } else {
                        if (height > MAX_HEIGHT) {
                            width = width * (MAX_HEIGHT / height);
                            height = MAX_HEIGHT;
                        }
                    }
                    }

                        // Dynamically create a canvas element
                        var canvas = document.createElement("canvas");
                    canvas.width = width;
                    canvas.height = height;
                        // var canvas = document.getElementById("canvas");
                        var ctx = canvas.getContext("2d");

                        // Actual resizing
                        ctx.drawImage(img, 0, 0, width, height);

                        // Show resized image in preview element
                        var dataurl = canvas.toDataURL(imageFile.type);

                        document.getElementById("idImage").src = dataurl;
                        document.getElementById("idPreviewImage").src = dataurl;
                        document.getElementById("hiddenField").setAttribute("value", dataurl);
                    }
                    img.src = e.target.result;
                }
               /* }else{
                var reader = new FileReader();

                  reader.onload = function (e) {

                  document.getElementById("idPreviewImage").src = e.target.result;
                  document.getElementById("idImage").src = e.target.result;

                  document.getElementById("hiddenField").setAttribute("value", e.target.result);

                  };
                  }*/

                reader.readAsDataURL(imageFile);
            //}
       // });




/*
 var file = files[0];

  if (!/^image/.test(file.type)) {
       alert('Выбранный файл не является изображением!');
       return;
     }
     console.log(file.size/1024);

console.log(file.size/1024);
  var reader = new FileReader();

  reader.onload = function (e) {
    // Используем URL изображения для заполнения фона
	//dropBox.style.backgroundImage = "url('" + e.target.result + "')";
  base64src = e.target.result;

  document.getElementById("idPreviewImage").src = base64src;
  document.getElementById("idImage").src = base64src;

document.getElementById("hiddenField").setAttribute("value", base64src);

  };

  // Начинаем считывать изображение
  reader.readAsDataURL(file);*/
}









