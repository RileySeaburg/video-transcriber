function showSuccess() {
	var success = $('#success').val();
	if (success) {
		toastr.success('We will notify you once transcription is complete.', 'Video transcription in progress!', { "showMethod": "slideDown", "hideMethod": "slideUp", timeOut: 5000 }); 
	}   
}