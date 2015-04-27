import grails.plugin.multitenantsingledbquartz2integration.*

class MultiTenantSingleDbQuartz2IntegrationGrailsPlugin {
	def version = '0.3'
	def grailsVersion = "2.0 > *"
	def pluginExcludes = ['grails-app/views/error.gsp']
	def title = 'Multi Tenant Single Db Quartz2 Integration Plugin'
	def author = 'Alex Anderson'
	def authorEmail = ''
	def description = 'Integration between Quartz2 and Multi-Tenant-Single-Db plugins to allow jobs to be run in the context of a Tenant.'
	def documentation = 'http://grails.org/plugin/multi-tenant-single-db-quartz2-integration'
	def license = 'APACHE'
	def loadAfter = ['quartz2']
	def organization = [name:'FrontlineSMS', url:'http://www.frontlinesms.com']
	def issueManagement = [system:'github', url:'https://github.com/frontlinesms/grails-multi-tenant-single-db-quartz2-integration-plugin/issues']
	def scm = [url:'git@github.com:frontlinesms/grails-multi-tenant-single-db-quartz2-integration-plugin.git']

	private def withCurrentTenantAwareness(def currentTenant, Map params, Closure c) {
		params['grails-multi-tenant-single-db-tenant-id'] = currentTenant.get()
		c.call(params)
	}

	def doWithSpring = {
		quartzJobFactory(TenantAwareJobFactory)
	}

	def doWithDynamicMethods = { ctx ->
		def currentTenant = ctx.getBean('currentTenant')

		application.jobClasses.each { jc ->
			def oldMethod = jc.metaClass.getStaticMetaMethod('triggerNow', [Map] as Class[])
			jc.metaClass.static.triggerNow = { Map params = null ->
				params = params?: [:]
				withCurrentTenantAwareness currentTenant, params, {
					oldMethod.invoke(null, params)
				}
			}

			// TODO decorate all the other job scheduling methods as well
		}
	}
}

