<html>
<head>
    <jsp:include page="WEB-INF/page/common_head.jsp"></jsp:include>
</head>
<body>
<div class="container">
    <div class="row"></div>
    <div class="row">
        <div class="col-md-4"></div>
        <div class="col-md-4">
            <form class="form-signin" action="testController/login.do">
                <h2 class="form-signin-heading">Please sign in</h2>

                <div class="form-group">
                    <label for="username" class="sr-only">User Accout</label>
                    <input type="text" id="username" class="form-control" placeholder="User Account" required
                           autofocus>
                </div>
                <div class="form-group">
                    <label for="inputPassword" class="sr-only">Password</label>
                    <input type="password" id="inputPassword" class="form-control" placeholder="Password" required>
                </div>
                <div class="checkbox">
                    <label>
                        <input type="checkbox" value="remember-me"> Remember me
                    </label>
                </div>
                <button class="btn btn-lg btn-primary btn-block" type="submit">Sign in</button>
            </form>
        </div>
        <div class="col-md-4"></div>
    </div>
    <div class="row"></div>
</div>
</body>
</html>
