 class VideoPlayer {
            constructor() {
                this.modal = document.getElementById('videoModal');
                this.videoElement = document.getElementById('modalVideo');
                this.player = null;
                this.initEventListeners();
            }

            initEventListeners() {
                // Обработчики для миниатюр видео
                document.querySelectorAll('.video-item').forEach(item => {
                    item.addEventListener('click', (e) => {
                        const videoSrc = item.getAttribute('data-video-src');
                        const poster = item.getAttribute('data-poster');
                        this.openModal(videoSrc, poster);
                    });
                });

                // Закрытие модального окна
                document.querySelector('.close-btn').addEventListener('click', () => this.closeModal());
                this.modal.addEventListener('click', (e) => {
                    if (e.target === this.modal) this.closeModal();
                });
                document.addEventListener('keydown', (e) => {
                    if (e.key === 'Escape') this.closeModal();
                });
            }

            openModal(videoSrc, poster) {
                if (!videoSrc) {
                    console.error('Video source not found');
                    return;
                }

                // Сначала показываем модальное окно
                this.modal.classList.add('active');
                document.body.style.overflow = 'hidden';

                // Инициализируем/обновляем Plyr
                if (!this.player) {
                    this.player = new Plyr('#modalVideo', {
                        controls: ['play-large', 'play', 'progress', 'current-time', 'mute', 'volume', 'fullscreen'],
                        hideControls: false,
                        settings: ['quality', 'speed'],
                        ratio: '16:9',
                        tooltips: {
                                controls: false,  // Отключаем стандартные подсказки
                                seek: false       // Отключаем подсказки при наведении на прогресс-бар
                            },
                        autoplay: false, // Лучше отключить или добавить muted: true
                        clickToPlay: true,
                        storage: { enabled: false }

                    });
                }

                // Устанавливаем источник через Plyr API
                this.player.source = {
                    type: 'video',
                    sources: [{
                        src: videoSrc,
                        type: 'video/mp4'
                    }],
                    poster: poster
                };

                // Пробуем воспроизвести с обработкой ошибок
                const playPromise = this.player.play();

                if (playPromise !== undefined) {
                    playPromise.catch(error => {
                        console.log('Autoplay prevented:', error);
                        // Можно показать кнопку "Нажмите для воспроизведения"
                    });
                }
            }

            closeModal() {
                if (this.player) {
                    this.player.pause();
                }
                this.modal.classList.remove('active');
                document.body.style.overflow = 'auto';
            }
        }

        // Инициализация при загрузке страницы
        document.addEventListener('DOMContentLoaded', () => {
            new VideoPlayer();
        });


