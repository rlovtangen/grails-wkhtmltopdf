import org.codehaus.groovy.grails.commons.DefaultGrailsControllerClass
import org.apache.catalina.core.ApplicationContext

class WkhtmltoxGrailsPlugin {
    // the plugin version
    def version = "0.1.5"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3.6 > *"
    // the other plugins this plugin depends on
    def dependsOn = ['mail': '1.0 > *',pluginConfig: '0.1.4 > *']
    def loadAfter = ['mail','controllers'];
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    def author = "Tobias Nendel"
    def authorEmail = "tobias.nendel@scubical.com"
    def title = "Wkhtmltopdf"
    def description = '''\\
This Plugin provides a Wrapper for wkhtmltopdf,
a Simple shell utility to convert html to pdf using the webkit rendering engine, and qt.

        render( filename:"Filename ${filename}.pdf",view:"/path_to_gsp",
                model:[someModel:someModel],
                header:"/path_to_gsp",
                footer:"/path_to_gsp",
                marginLeft:20,
                marginTop:30,
                marginBottom:20,
                marginRight:20,
                headerSpacing:10
        )
'''
    def license = "APACHE"

    def observe = ['controllers']

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/wkhtmltox"

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before 
    }

    def doWithSpring = {
        // TODO Implement runtime spring config (optional)
    }

    def doWithDynamicMethods = {ctx ->

        // hooking into render method
        application.controllerClasses.each() {controllerClass ->
            replaceRenderMethod(controllerClass)
        }
    }

    def onChange = {event ->

        // only process controller classes
        if (application.isArtefactOfType(DefaultGrailsControllerClass.CONTROLLER, event.source)) {
            def clazz = application.getControllerClass(event.source?.name)
            replaceRenderMethod(clazz)
        }
    }

    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }



    /**
     * This implementation is based on Marc Palmers feed plugin. It hooks into the render method
     * of a Grails controller class and adds an alternative behaviour for the mime type
     * 'text/calendar' used by the iCalendar plugin.
     */
    private void replaceRenderMethod(controllerClass) {
        def oldRender = controllerClass.metaClass.pickMethod("render", [Map] as Class[])
        controllerClass.metaClass.render = {Map params ->
            if (params.contentType?.toLowerCase() == 'application/x-pdf' || response.format == "pdf") {
                def filename = params.remove("filename")

                def data = grailsApplication.mainContext.getBean('wkhtmltoxService').makePdf(params)

                response.setHeader("Content-disposition", "attachment; filename=${filename}")
                response.contentType = "application/x-pdf"
                response.outputStream.write(data)
                response.characterEncoding = 'UTF-8'
                response.setHeader('Cache-Control', 'no-store, no-cache, must-revalidate') //HTTP/1.1
                response.setHeader('Pragma', 'no-cache') // HTTP/1.0
                response.outputStream.flush()

            } else {
                // Defer to original render method
                oldRender.invoke(delegate, [params] as Object[])
            }
        }
    }

}
