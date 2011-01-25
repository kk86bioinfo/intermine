package org.intermine.web.struts;

/*
 * Copyright (C) 2002-2011 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.io.StringReader;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.intermine.api.InterMineAPI;
import org.intermine.api.bag.BagManager;
import org.intermine.api.profile.InterMineBag;
import org.intermine.api.profile.Profile;
import org.intermine.api.query.WebResultsExecutor;
import org.intermine.api.results.WebResults;
import org.intermine.pathquery.PathQuery;
import org.intermine.pathquery.PathQueryBinding;
import org.intermine.web.logic.config.WebConfig;
import org.intermine.web.logic.export.http.TableExporterFactory;
import org.intermine.web.logic.export.http.TableHttpExporter;
import org.intermine.web.logic.results.PagedTable;
import org.intermine.web.logic.session.SessionMethods;

/**
 * Implementation of <strong>Action</strong> that runs a template
 *
 * @author Julie Sullivan
 */
public class LoadTemplateAction extends DispatchAction
{
    /**
     * Load a query from path query XML passed as a request parameter.
     * @param mapping The ActionMapping used to select this instance
     * @param form The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     * @return an ActionForward object defining where control goes next
     * @exception Exception if the application business logic throws
     *  an exception
     */
    public ActionForward method(ActionMapping mapping,
                             ActionForm form,
                              HttpServletRequest request,
                              HttpServletResponse response)
        throws Exception {
        HttpSession session = request.getSession();
        final InterMineAPI im = SessionMethods.getInterMineAPI(session);
        Profile profile = SessionMethods.getProfile(session);
        String trail = request.getParameter("trail");
        String queryXml = request.getParameter("query");
        Boolean skipBuilder = Boolean.valueOf(request.getParameter("skipBuilder"));
        String exportFormat = request.getParameter("exportFormat");


        PathQuery query = PathQueryBinding.unmarshalPathQuery(new StringReader(queryXml),
                PathQuery.USERPROFILE_VERSION);
        BagManager bagManager = im.getBagManager();

        Map<String, InterMineBag> allBags = bagManager.getUserAndGlobalBags(profile);
        for (String bagName : query.getBagNames()) {
            if (!allBags.containsKey(bagName)) {
                throw new RuntimeException("Saved bag (list) '" + bagName + "' not found for "
                        + "profile: " + profile.getUsername() + ", referenced in query: " + query);
            }
        }

        if (exportFormat == null) {
            SessionMethods.loadQuery(query, session, response);
            if (!skipBuilder.booleanValue()) {
                return mapping.findForward("query");
            } else {
                String qid = SessionMethods.startQueryWithTimeout(request, false, query);
                Thread.sleep(200); // slight pause in the hope of avoiding holding page
                return new ForwardParameters(mapping.findForward("waiting"))
                                   .addParameter("trail", trail)
                                   .addParameter("qid", qid).forward();
            }
        } else {
            PagedTable pt = null;
            try {
                WebResultsExecutor executor = im.getWebResultsExecutor(profile);
                pt = new PagedTable(executor.execute(query));

                if (pt.getWebTable() instanceof WebResults) {
                    ((WebResults) pt.getWebTable()).goFaster();
                }

                WebConfig webConfig = SessionMethods.getWebConfig(request);
                TableExporterFactory factory = new TableExporterFactory(webConfig);

                TableHttpExporter exporter = factory.getExporter(exportFormat);

                if (exporter == null) {
                    throw new RuntimeException("unknown export format: " + exportFormat);
                }

                exporter.export(pt, request, response, null);

                // If null is returned then no forwarding is performed and
                // to the output is not flushed any jsp output, so user
                // will get only required export data
                return null;
            } finally {
                if (pt != null && pt.getWebTable() instanceof WebResults) {
                    ((WebResults) pt.getWebTable()).releaseGoFaster();
                }
            }
        }
    }
}