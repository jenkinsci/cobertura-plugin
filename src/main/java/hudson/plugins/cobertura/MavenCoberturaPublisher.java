package hudson.plugins.cobertura;

import hudson.Extension;
import hudson.FilePath;
import hudson.maven.MavenBuild;
import hudson.maven.MavenBuildProxy;
import hudson.maven.MavenModule;
import hudson.maven.MavenReporter;
import hudson.maven.MavenReporterDescriptor;
import hudson.maven.MojoInfo;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.plugins.cobertura.renderers.SourceCodePainter;
import hudson.plugins.cobertura.renderers.SourceEncoding;
import hudson.plugins.cobertura.targets.CoverageResult;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * Created by IntelliJ IDEA. User: stephen Date: 17-Nov-2007 Time: 19:08:46
 */
public class MavenCoberturaPublisher extends MavenReporter {

    @Override
    public boolean preExecute(MavenBuildProxy build, MavenProject pom, MojoInfo mojo, BuildListener listener) throws InterruptedException,
            IOException {
        if (isCoberturaReport(mojo)) {
            boolean maven3orLater = maven3orLater( build.getMavenBuildInformation().getMavenVersion());
            // tell cobertura:cobertura to generate the XML report
            PlexusConfiguration c = mojo.configuration.getChild("formats");
            if (c == null) {
                listener.getLogger().println("[JENKINS] Configuring cobertura-maven-plugin to enable xml reports");

                Xpp3Dom fmts = new Xpp3Dom("formats");
                Xpp3Dom fmt = new Xpp3Dom("format");
                fmt.setValue("html"); // this is in by default
                fmts.addChild(fmt);
                Xpp3Dom fmt2 = new Xpp3Dom("format");
                fmt2.setValue("xml"); // need this
                fmts.addChild(fmt2);

                if (!maven3orLater) {
                    XmlPlexusConfiguration xmlPlexusConfiguration = new XmlPlexusConfiguration(fmts);
                    mojo.configuration.addChild(xmlPlexusConfiguration);
                } else {
                    mojo.mojoExecution.setConfiguration( fmts );
                }
            } else {
                PlexusConfiguration[] fmts = c.getChildren("format");
                boolean xmlConfigured = false;
                for (PlexusConfiguration fmt : fmts) {
                    if ("xml".equalsIgnoreCase(fmt.getValue().trim())) {
                        xmlConfigured = true;
                        break;
                    }
                }
                if (xmlConfigured) {
                    listener.getLogger().println("[JENKINS] cobertura-maven-plugin already configured with xml reports enabled");
                } else {
                    listener.getLogger().println("[JENKINS] Configuring cobertura-maven-plugin to enable xml reports");
                    if (!maven3orLater) {
                        XmlPlexusConfiguration fmt = new XmlPlexusConfiguration("format");
                        fmt.setValue("xml"); // need this
                        c.addChild(fmt);
                    } else {
                       Xpp3Dom fmt = new Xpp3Dom("format");
                       fmt.setValue("xml");
                       Xpp3Dom formats = mojo.mojoExecution.getConfiguration().getChild( "formats" );
                       if (formats == null) {
                           formats = new Xpp3Dom( "formats" );
                           mojo.mojoExecution.getConfiguration().addChild( formats );
                       }
                       formats.addChild( fmt );
                    }
                }
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean postExecute(final MavenBuildProxy build, final MavenProject pom, final MojoInfo mojo, final BuildListener listener,
            final Throwable error) throws InterruptedException, IOException {
        if (!isCoberturaReport(mojo))
            return true;

        boolean haveXMLReport = false;
        File outputDir;
        try {
            outputDir = mojo.getConfigurationValue("outputDirectory", File.class);
            if (outputDir == null || !outputDir.exists()) {
                // cobertura-maven-plugin will not generate a report for
                // non-java projects
                return true;
            }

            String[] formats = mojo.getConfigurationValue("formats", String[].class);
            if (formats != null) {
                for (String o : formats) {
                    if ("xml".equalsIgnoreCase(o.trim())) {
                        haveXMLReport = true;
                        break;
                    }
                }
            }
            if (!haveXMLReport) {
                listener.getLogger().println("[JENKINS] I could not auto-configure the cobertura-maven-plugin to generate xml reports");
                listener.getLogger().println("[JENKINS] If the cobertura plugin needs was configured to generate xml reports, e.g.");
                listener.getLogger().println("[JENKINS]     ...");
                listener.getLogger().println("[JENKINS]     <plugin>");
                listener.getLogger().println("[JENKINS]       <groupId>org.codehaus.mojo</groupId>");
                listener.getLogger().println("[JENKINS]       <artifactId>codehaus-maven-plugin</artifactId>");
                listener.getLogger().println("[JENKINS]       ...");
                listener.getLogger().println("[JENKINS]       <configuration>");
                listener.getLogger().println("[JENKINS]         ...");
                listener.getLogger().println("[JENKINS]         <formats>");
                listener.getLogger().println("[JENKINS]           ...");
                listener.getLogger().println("[JENKINS]           <format>xml</format> <!-- ensure this format is present -->");
                listener.getLogger().println("[JENKINS]           ...");
                listener.getLogger().println("[JENKINS]         </formats>");
                listener.getLogger().println("[JENKINS]         ...");
                listener.getLogger().println("[JENKINS]       </configuration>");
                listener.getLogger().println("[JENKINS]       ...");
                listener.getLogger().println("[JENKINS]     </plugin>");
                listener.getLogger().println("[JENKINS]     ...");
                listener.getLogger().println("[JENKINS] Code coverage reports would be enabled");
                build.setResult(Result.UNSTABLE);
                return true;
            }
        } catch (ComponentConfigurationException e) {
            e.printStackTrace(listener.fatalError("Unable to obtain configuration from cobertura mojo"));
            build.setResult(Result.UNSTABLE);
            return true;
        }

        File reportFile = new File(outputDir, "coverage.xml");
        if (!reportFile.exists()) {
            listener.getLogger().println("[JENKINS] Cobertura report not generated (probably this module is not a java module)");
            return true;
        }

        String coverageXml = FileUtils.readFileToString(reportFile);
        if (!build.execute(new MavenCoberturaActionAdder(listener, coverageXml, pom.getBasedir().getAbsolutePath()))) {
            listener.getLogger().println("[JENKINS] Unable to add link to cobertura results");
            build.setResult(Result.FAILURE);
            return true;
        }

        build.registerAsProjectAction(this);

        return true;
    }

    private boolean isCoberturaReport(MojoInfo mojo) {
        if (!mojo.pluginName.matches("org.codehaus.mojo", "cobertura-maven-plugin") && !mojo.pluginName.matches("org.codehaus.mojo", "cobertura-it-maven-plugin"))
            return false;

        if (!mojo.getGoal().equals("cobertura"))
            return false;
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<? extends Action> getProjectActions(MavenModule project) {
        return Collections.singleton(new CoberturaProjectAction(project.getLastBuild()));
    }

    @Extension(optional = true)
    public static final class DescriptorImpl extends MavenReporterDescriptor {

        /**
         * {@inheritDoc}
         */
        public String getDisplayName() {
            return Messages.MavenCoberturaPublisher_displayName();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public MavenReporter newAutoInstance(MavenModule mavenModule) {
            return new MavenCoberturaPublisher();
        }

    }

    private static final long serialVersionUID = 1L;

    private static class MavenCoberturaActionAdder implements MavenBuildProxy.BuildCallable<Boolean, IOException> {
        private static final long serialVersionUID = -5470450037371279762L;
        @SuppressWarnings("unused")
        private final BuildListener listener;
        private final String coverageXml;
        private final String workspace;

        MavenCoberturaActionAdder(BuildListener listener, String coverageXml, String workspace) {
            this.listener = listener;
            this.coverageXml = coverageXml;
            this.workspace = workspace;
        }

        public Boolean call(MavenBuild build) throws IOException {
            final FilePath paintedSourcesPath = new FilePath(build.getParent().getRootDir()).child("cobertura");
            try {
                CoberturaBuildAction cba = build.getAction(CoberturaBuildAction.class);
                if (cba == null) {
                    File cvgxml = new File(build.getRootDir(), "coverage.xml");
                    FileUtils.write(cvgxml, coverageXml);
                    Set<String> sourcePaths = new HashSet<>();
                    CoverageResult result = CoberturaCoverageParser.parse(cvgxml, null, sourcePaths);
                    result.setOwner(build);

                    CoberturaBuildAction o = CoberturaBuildAction.load(result, null, null, false, false, false, false, false, false, 0);
                    build.addAction(o);

                    // Get System default encoding;
                    SourceEncoding encoding = SourceEncoding.getEncoding(System.getProperty("file.encoding"));
                    new SourceCodePainter(sourcePaths, result.getPaintedSources(), listener, encoding).paint(new FilePath(build.getWorkspace().getChannel(), workspace), paintedSourcesPath);
                } else {
                    return false;
                }
            } catch (NullPointerException e) {
                return false;
            } catch (InterruptedException x) {
                throw new IOException(x);
            }
            return true;
        }
    }

    public boolean maven3orLater(String mavenVersion) {
        // null or empty so false !
        if (StringUtils.isBlank( mavenVersion )) {
            return false;
        }
        return new ComparableVersion (mavenVersion).compareTo( new ComparableVersion ("3.0") ) >= 0;
    }
}
