// При загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);

    if (urlParams.has('success')) {
        showMessage('✅ Сообщение успешно отправлено!', 'success');
        // Удаляем параметр из URL (опционально)
        window.history.replaceState({}, '', window.location.pathname);
    }

    if (urlParams.has('error')) {
        const errorType = urlParams.get('error');
        if (errorType === 'mail') {
            showMessage('❌ Ошибка при отправке письма', 'error');
        } else if (errorType === 'validation') {
            showMessage('❌ Ошибка валидации', 'error');
        }
        window.history.replaceState({}, '', window.location.pathname);
    }
});

function showMessage(message, type) {
    const loaderDiv = document.getElementById('loader');
    if (!loaderDiv) return;

    // Убираем класс hidden (показываем)
    loaderDiv.classList.remove('hidden');

    // Убираем предыдущие классы стилей
    loaderDiv.classList.remove('success', 'error', 'info');

    // Добавляем класс в зависимости от типа
    if (type === 'success') {
        loaderDiv.classList.add('success');
    } else if (type === 'error') {
        loaderDiv.classList.add('error');
    } else {
        loaderDiv.classList.add('info');
    }

    // Устанавливаем текст
    loaderDiv.textContent = message;

    // Через 10 секунд скрываем
    setTimeout(() => {
        loaderDiv.classList.add('hidden');
        loaderDiv.textContent = '';
    }, 10000);
}

function messageMail() {

 var loaderDiv = document.getElementById('loader');
 loaderDiv.classList.remove('success', 'error', 'info');
 loaderDiv.classList.remove('hidden');
 loaderDiv.classList.add('info');
 loaderDiv.innerHTML="Отправка письма...";

  //document.getElementById("loader").setAttribute("value","Отправка письма...");
}
