package grails.plugin.multitenantsingledbquartz2integration

import org.quartz.*
import org.quartz.spi.TriggerFiredBundle
import grails.plugin.quartz2.GrailsJobFactory

import grails.plugin.multitenant.core.CurrentTenant
import grails.plugin.multitenant.core.MultiTenantService

import org.springframework.beans.factory.annotation.Autowired

/**
 * This class wraps job execution calls to make sure that they have CurrentTenant set.
 * As far as I can work out, this class is only necessary because of https://jira.codehaus.org/browse/GROOVY-3493
 * prevents wrapping execute() of GrailsArtefactJob directly in Plugin.doWithDynamicMethods().
 */
class TenantAwareJobFactory extends GrailsJobFactory {
	@Autowired CurrentTenant currentTenant
	@Autowired MultiTenantService multiTenantService

	public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
		final Job wrappedJob = super.newJob(bundle, scheduler)
		return new Job() {
			public void execute(final JobExecutionContext context) throws JobExecutionException {
				Integer tenantId = context.getMergedJobDataMap().getIntValue("grails-multi-tenant-single-db-tenant-id")
				// TODO remove tenantId from the mergedJobData
				multiTenantService.doWithTenantId tenantId, {
					wrappedJob.execute(context)
				}
			}
		};
	}
}

