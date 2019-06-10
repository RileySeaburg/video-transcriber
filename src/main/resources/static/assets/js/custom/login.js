function validateLogin(){
	var email = $('#email').val();
	if (!email || email == "") {
		$("#email").addClass('is-invalid');
		return false;
	} else{
		$("#email").removeClass('is-invalid');
	}
	var password = $('#password').val();
	if (!password || password == "") {
		$("#password").addClass('is-invalid');
		return false;
	} else{
		$("#password").removeClass('is-invalid');
	}
	return true;
}