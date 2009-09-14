@echo Running the source from ./package/
pushd package
java -cp FrontlineSMS.jar;masabiFormsMessageHandler.jar;comm.jar net.frontlinesms.DesktopLauncher
popd