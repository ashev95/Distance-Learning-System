<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <link rel="shortcut icon" href="resources/dojo/images/favicon.ico" type="image/x-icon">
    <title>Система дистанционного обучения</title>

    <!-- -
      <link rel="stylesheet" href="//ajax.googleapis.com/ajax/libs/dojo/1.12.2/dijit/themes/claro/claro.css">
    <link rel="stylesheet" href="//ajax.googleapis.com/ajax/libs/dojo/1.12.2/dojo/resources/dojo.css">
    <link rel="stylesheet" href="//ajax.googleapis.com/ajax/libs/dojo/1.12.2/dojox/grid/resources/Grid.css">
    <link rel="stylesheet" href="//ajax.googleapis.com/ajax/libs/dojo/1.12.2/dojox/grid/resources/claroGrid.css">
    -->

      <link rel="stylesheet" href="resources/dojo/1.12.2/dijit/themes/claro/claro.css">
      <link rel="stylesheet" href="resources/dojo/1.12.2/dojo/resources/dojo.css">
      <link rel="stylesheet" href="resources/dojo/1.12.2/dojox/grid/resources/Grid.css">
      <link rel="stylesheet" href="resources/dojo/1.12.2/dojox/grid/resources/claroGrid.css">

      <link rel="stylesheet" href="resources/dojo/style.css">

  </head>
  <body class="claro">

      <script>

          var windowName = '<%=session.getAttribute("windowNameToSet")%>';

          <%if (session.getAttribute("windowNameToSet") != null) {
                session.removeAttribute("windowNameToSet");
          }%>

          if (windowName != "<%=session.getAttribute("windowName")%>") {
              window.location = "<%=request.getContextPath()%>/expired";
          }

          function checkSession(){
              dojo.xhrGet({
                  url: '/session_checking/' + windowName,
                  error: function(request, ioArgs) {
                      location.href = window.location.origin + '/expired';
                  }
              });
              setTimeout(checkSession, 30000);
          }
          setTimeout(checkSession, 30000);

          var dojoConfig = {
              parseOnLoad: true,
              async: true,
              dojoBlankHtmlUrl: '/resources/dojo/html/blank.html',
              packages: [{
                  name: 'widgets',
                  location: '/resources/dojo/js/custom/widgets'
              }]
          };

      </script>

      <!-- <script src="//ajax.googleapis.com/ajax/libs/dojo/1.12.2/dojo/dojo.js"></script> -->
      <script src="/resources/dojo/1.12.2/dojo/dojo.js"></script>
      <script>
          require(["widgets/AppWidget"]);
      </script>
      <div data-dojo-type="AppWidget"></div>

  </body>

</html>