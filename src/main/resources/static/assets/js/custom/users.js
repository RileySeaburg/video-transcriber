$('#user_table').DataTable();
//console.log('Hi')

function validateUser(){
	var firstName = $('#firstName').val();
	if (!firstName || firstName == "") {
		$("#firstName").addClass('is-invalid');
		return false;
	} else{
		$("#firstName").removeClass('is-invalid');
	}
	
	var lastName = $('#lastName').val();
	if (!lastName || lastName == "") {
		$("#lastName").addClass('is-invalid');
		return false;
	} else{
		$("#lastName").removeClass('is-invalid');
	}
	
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