call mvn clean install

call rd ..\..\..\..\dist\examples\helloworld\server /s /q
call xcopy target\dist\tio-examples-helloworld-server-1.6.8.v20170329-RELEASE ..\..\..\..\dist\examples\helloworld\server\ /s /e /q /y

