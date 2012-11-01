/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package crash.commands.base

import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Command
import org.crsh.command.InvocationContext
import javax.naming.InitialContext
import org.crsh.command.CRaSHCommand
import javax.naming.Context
import org.crsh.text.formatter.BindingRenderable
import org.crsh.text.formatter.BindingRenderable.BindingData
import org.crsh.util.TypeResolver
import org.crsh.cmdline.annotations.Option

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
@Usage("Java Naming and Directory Interface")
class jndi extends CRaSHCommand {

    @Usage("List JNDI resources")
    @Command
    void find(
            InvocationContext<BindingRenderable.BindingData> context,
            @Usage("Filter displayed resources using FQN type'") @Option(names=["f","filter"]) List<String> filters,
            @Usage("Display resource type'") @Option(names=["v", "verbose"]) Boolean verbose,
            @Usage("Apply a filter on 'javax.sql.DataSource'") @Option(names=["d", "datasources"]) Boolean datasources,
            @Usage("Apply a filter on 'javax.persistence.EntityManagerFactory'") @Option(names=["e", "emf"]) Boolean emf,
            @Usage("Apply a filter on 'javax.mail.Session'") @Option(names=["m", "mail"]) Boolean mail) {

        if (datasources) {
            if (filters == null) filters = new ArrayList<String>();
            filters.add("javax.sql.DataSource");
        }

        if (emf) {
            if (filters == null) filters = new ArrayList<String>();
            filters.add("javax.persistence.EntityManagerFactory");
        }

        if (mail) {
            if (filters == null) filters = new ArrayList<String>();
            filters.add("javax.mail.Session");
        }

        add(context, filters, verbose, "");
        add(context, filters, verbose, "java:/");
        add(context, filters, verbose, "java:comp/env/jdbc");
        add(context, filters, verbose, "java:jboss");
        add(context, filters, verbose, "java:global");
        add(context, filters, verbose, "java:app");
        add(context, filters, verbose, "java:module");

    }

    void add(InvocationContext<BindingRenderable.BindingData> context, List<String> filters, Boolean verbose, String path) {
        add(context, filters, verbose, path, path, null)
    }

    void add(InvocationContext<BindingRenderable.BindingData> context, List<String> filters, Boolean verbose, String path, String search, Context ctx) {
        try {
            if (ctx == null) {
                ctx = new InitialContext();
            }
            if (path.length() > 0) {
                path += "/";
            }

            ctx.listBindings(search).each { instance ->
                try {
                    if (
                        filters == null ||
                        filters.size() == 0 ||
                        TypeResolver.instanceOf(instance.object.class, filters)) {
                            context.provide(new BindingData(path + instance.name, instance.className, (verbose ? true : false)));
                    }
                } catch (ClassCastException e) {}
                if (instance.object instanceof Context) {
                    add(context, filters, verbose, path + instance.name, "", instance.object);
                }
            }
        } catch(Exception e) {}
    }

}