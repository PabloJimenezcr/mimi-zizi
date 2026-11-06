(() => {
    'use strict';

    const input = document.querySelector('[data-mascota-photo-input]');
    const preview = document.querySelector('[data-mascota-photo-preview]');
    const image = document.querySelector('[data-mascota-photo-image]');
    const name = document.querySelector('[data-mascota-photo-name]');
    const removeButton = document.querySelector('[data-mascota-photo-remove]');
    const currentPhoto = document.querySelector('[data-mascota-photo-current]');
    const error = document.getElementById('foto-error');

    if (!input || !preview || !image || !removeButton) {
        return;
    }

    const maxSize = 5 * 1024 * 1024;
    const allowedTypes = ['image/jpeg', 'image/png'];

    let currentObjectUrl = null;

    const showError = (message) => {
        if (error) {
            error.textContent = message;
        }
    };

    const clearPreview = () => {

        if (currentObjectUrl) {
            URL.revokeObjectURL(currentObjectUrl);
            currentObjectUrl = null;
        }

        input.value = '';
        image.removeAttribute('src');
        name.textContent = '';
        preview.hidden = true;

        if (currentPhoto) {
            currentPhoto.hidden = false;
        }

        showError('');
    };

    input.addEventListener('change', () => {

        showError('');

        const file = input.files?.[0];

        if (!file) {
            clearPreview();
            return;
        }

        if (!allowedTypes.includes(file.type)) {
            clearPreview();
            showError('Selecciona una imagen JPG o PNG.');
            return;
        }

        if (file.size > maxSize) {
            clearPreview();
            showError('La fotografía no puede superar los 5 MB.');
            return;
        }

        if (currentObjectUrl) {
            URL.revokeObjectURL(currentObjectUrl);
        }

        currentObjectUrl = URL.createObjectURL(file);

        image.src = currentObjectUrl;
        name.textContent = file.name;
        preview.hidden = false;

        if (currentPhoto) {
            currentPhoto.hidden = true;
        }
    });

    removeButton.addEventListener('click', clearPreview);

    window.addEventListener('pagehide', () => {
        if (currentObjectUrl) {
            URL.revokeObjectURL(currentObjectUrl);
        }
    });

})();