## Changelog

### Version 1.17 (08-Nov-2021)

- added support for spcifying report file pattern via environment variables
- restored compatibility with https://plugins.jenkins.io/remoting-security-workaround
- cleaned up agent to controller code
- removed obsolete and long ago broken Maven integration

### Version 1.16 (03-Mar-2020)

- Address SECURITY-1668 from https://www.jenkins.io/security/advisory/2020-03-09/
- Address SECURITY-1700 from https://www.jenkins.io/security/advisory/2020-03-09/

### Version 1.15 (10-Oct-2019)

### Version 1.14 (12-Jun-2019)

### Version 1.13 (14-Sep-2018)

### Version 1.12.1 (10-May-2018)

- Failed to scout
    hudson.plugins.cobertura.MavenCoberturaPublisher ([JENKINS-44200](https://issues.jenkins-ci.org/browse/JENKINS-44200))
- Fix highlight for partially covered
    branches ([JENKINS-13489](https://issues.jenkins-ci.org/browse/JENKINS-13489))
- Don't round up 99.x% coverage to
    100% ([JENKINS-43866](https://issues.jenkins-ci.org/browse/JENKINS-43866))

### Version 1.12 (12-Nov-2017)

- Show why build failed when it missed coverage targets
    ([JENKINS-47639](https://issues.jenkins-ci.org/browse/JENKINS-47639))
- Prefix all logs with \`\[Cobertura\]\`
    ([JENKINS-25781](https://issues.jenkins-ci.org/browse/JENKINS-25781))
- Fix Phabricator compatibility regression ([Issue
    \#73](https://github.com/jenkinsci/cobertura-plugin/issues/73))
- Add support for pipeline snippet generator

### Version 1.11 (09-Aug-2017)

- Added pipline support for coverage targets ([Issue
    \#67](https://github.com/jenkinsci/cobertura-plugin/issues/67))
- Publish jobs when onlyStable is false even if job fails ([Issue
    \#59](https://github.com/jenkinsci/cobertura-plugin/issues/59))

### Version 1.10 (25-Apr-2017)

- Support Jenkins pipeline
    ([JENKINS-30700](https://issues.jenkins-ci.org/browse/JENKINS-30700))

- Avoid error when CoverageMetric EnumSet is empty
    ([JENKINS-6425](https://issues.jenkins-ci.org/browse/JENKINS-6425))
- Remove deprecated use of ChartUtil.generateGraph
    ([JENKINS-17800](https://issues.jenkins-ci.org/browse/JENKINS-17800))
- Fix typo in Spanish properties

### Version 1.9.8 (8-May-2016)

- Allow later concurrent builds to finish
    first ([JENKINS-26823](https://issues.jenkins-ci.org/browse/JENKINS-26823))
- Find code from Python
    coverage ([JENKINS-13889](https://issues.jenkins-ci.org/browse/JENKINS-13889))

### Version 1.9.7  (4-Mar-2015)

- Fixes broken dashboard links when inside folder
    ([JENKINS-26410](https://issues.jenkins-ci.org/browse/JENKINS-26410))

### Version 1.9.6 (25-Oct-2014)

- Fixed URL to coverage results in views and folders
    ([JENKINS-24436](https://issues.jenkins-ci.org/browse/JENKINS-24436))

### Version 1.9.5 (24-Apr-2014)

- Added coverage column that shows line/branch coverage in views

### Version 1.9.4 (17-Apr-2014)

- Fix display when data for one of the columns is missing
    ([JENKINS-22412](https://issues.jenkins-ci.org/browse/JENKINS-22412))

### Version 1.9.3 (16-Oct-2013)

- More fixes of file descriptor leaks

### Version 1.9.2 (9-Aug-2013)

- Cobertura Unable to delete coverage.xml on windows
    ([JENKINS-18858](https://issues.jenkins-ci.org/browse/JENKINS-18858)).

### Version 1.9.1 (14-Jun-2013)

- Added "most recent N builds" limiting option for coverage graph.
- Fixed columns order on
    dashboard([JENKINS-18218](https://issues.jenkins-ci.org/browse/JENKINS-18218)).

### Version 1.9 (28-Apr-2013)

- SourceCodePainter overwrites original files
    ([JENKINS-16252](https://issues.jenkins-ci.org/browse/JENKINS-16252)).
- table.source font-family should not specify courier
    ([JENKINS-3567](https://issues.jenkins-ci.org/browse/JENKINS-3567)).
- show greenbar collectly in IE Quirks Mode
    ([JENKINS-8568](https://issues.jenkins-ci.org/browse/JENKINS-8568)).
- There should be a cobertura summary item on the build status page
    ([JENKINS-8441](https://issues.jenkins-ci.org/browse/JENKINS-8441)).
- show legend under the graph.
- sort order of metrics. package, file, class, method, line,
    condition.
- Cobertura plugin does not provide data to the REST API
    ([JENKINS-13877](https://issues.jenkins-ci.org/browse/JENKINS-13877)).
- Cobertura ClassCastException
    ([JENKINS-15703](https://issues.jenkins-ci.org/browse/JENKINS-15703)).

### Version 1.8 (15-Dec-2012)

- Crop unusaged whitespace in coverage
    graph([JENKINS-16038](https://issues.jenkins-ci.org/browse/JENKINS-16038)).
- testing if workspace permissions
- fixed layout: added align="right" to be displayed collectly
- Cannot plublish cobertura reports if
    org.codehaus.mojo:cobertura-maven-plugin is not
    invoked([JENKINS-14552](https://issues.jenkins-ci.org/browse/JENKINS-14552)).
- Cobertura - add option to make build as unstable (or not at all)
    instead of failed when no coverage xml files are found
    ([JENKINS-12857](https://issues.jenkins-ci.org/browse/JENKINS-12857)).

### Version 1.7.1 (17-Oct-2012)

- fix regression
    [JENKINS-15518](https://issues.jenkins-ci.org/browse/JENKINS-15518)

### Version 1.7 (11-Oct-2012)

- Memory footprint reduction.
- [JENKINS-15035](https://issues.jenkins-ci.org/browse/JENKINS-15035)

### Version 1.6 (17-Aug-2012)

- Inconsistent delete
    button([JENKINS-14589](https://issues.jenkins-ci.org/browse/JENKINS-14589)).
- Allow the build to fail on low coverage
    ([JENKINS-11025](https://issues.jenkins-ci.org/browse/JENKINS-11025)).
- Support for ratcheting
    ([JENKINS-8326](https://issues.jenkins-ci.org/browse/JENKINS-8326)).
- include support for
    ['cobertura-it-maven-plugin'](http://code.google.com/p/cobertura-it-maven-plugin/).

### Version 1.5 (20-May-2012)

- Code Coverages dashboard portlet missing column("METHODS")
    ([JENKINS-7366](https://issues.jenkins-ci.org/browse/JENKINS-7366)).
- cobertura coverage dashboard portlet not using numeric sort for
    percent columns
    ([JENKINS-13250](https://issues.jenkins-ci.org/browse/JENKINS-13250)).
- updated Japanese localization.
- some fixes.

### Version 1.4 (5-May-2012)

- cobertura conditionals not available with a French server + regexps
    optimizations
    ([JENKINS-7540](https://issues.jenkins-ci.org/browse/JENKINS-7540)).
- Cobertura gives LinkageError in new Jenkins version
    ([JENKINS-11398](https://issues.jenkins-ci.org/browse/JENKINS-11398)).
- Cobertura plugin should not fail maven build for maven release
    ([JENKINS-12640](https://issues.jenkins-ci.org/browse/JENKINS-12640))
    ([pull-6](https://github.com/jenkinsci/cobertura-plugin/pull/6)).

### Version 1.3 (13-Aug-2011)

- Change so output format will be in alphabetical order by default
- Put \<pre\>...\</pre\> tags around source code content in case
    cobertura directory is linked to source code
- Added description of the Source Encoding

### Version 1.2 (25-Feb-2011)

- Update for Jenkins

### Version 1.1 (11-Jan-2011)

- Fix <http://issues.jenkins-ci.org/browse/JENKINS-8362> : cobertura
    plugin and maven3.

### Version 1.0 (30-Jul-2010)

- Fix so 0/0 is counted as 100% instead of 0% coverage (ie, a method
    with no conditionals).
    ([JENKINS-6790](https://issues.jenkins-ci.org/browse/JENKINS-6790))
- Fix in source viewer so "\\n" and "\\r" (backslash+n/r, not actual
    newlines) are not omitted.
    ([JENKINS-3566](https://issues.jenkins-ci.org/browse/JENKINS-3566))
- Add support for dashboard plugin

### Version 0.8.11 (22-Mar-2010)

- Fixed: source code unavailable when unstable
    ([JENKINS-4803](https://issues.jenkins-ci.org/browse/JENKINS-4803))
- Fixed an issue in internationalization on static enum clases which
    made some texts be shown in English.
- Fixed a bug in the way the tables were sorted (same problem than
    emma
    [JENKINS-4173](https://issues.jenkins-ci.org/browse/JENKINS-4173)).
    Now they are sorted numerically instead of alphabetically.
- Added Spanish internationalization.

### Version 0.8.10 (15-Jan-2010)

- Reorganize data structures to allow processing larger result files
- Use EnumMap and EnumSet for more compact in-memory representation of
    data
- Update code for more recent Hudson
- Change report colors as described
    [here](http://n4.nabble.com/cobertura-plugin-color-change-td932633.html)
- Internationalize messages
    ([JENKINS-4920](https://issues.jenkins-ci.org/browse/JENKINS-4920))

### Version 0.8.9 (8-Jul-2009)

- Added green/red results bars to statistic blocks
    ([JENKINS-3869](https://issues.jenkins-ci.org/browse/JENKINS-3869))
- Improved support for multi-module SCMs other than Subversion (such
    as CVS)
    ([JENKINS-1323](https://issues.jenkins-ci.org/browse/JENKINS-1323))
- Fixed an issue that broke source highlighting for module build
    result pages
    ([JENKINS-3938](https://issues.jenkins-ci.org/browse/JENKINS-3938))

### Version 0.8.8 (11-Jun-2009)

- Revert the memory usage fixes in 0.8.7, since they were breaking
    source highlighting
    ([JENKINS-3597](https://issues.jenkins-ci.org/browse/JENKINS-3597))

### Version 0.8.7 (4-Jun-2009)

- Improved help and error messages to attempt to avoid "Can not find
    coverage-results"
    ([JENKINS-1423](https://issues.jenkins-ci.org/browse/JENKINS-1423))
- Fixed "Consider only stable builds" setting
    ([JENKINS-3475](https://issues.jenkins-ci.org/browse/JENKINS-3475))
- Improved memory usage when drawing trend graphs
    ([JENKINS-3597](https://issues.jenkins-ci.org/browse/JENKINS-3597))

### Version 0.8.6 (7-May-2009)

- The plugin runs before notifications are sent out, to avoid
    inconsistency in build status reporting
    ([JENKINS-1285](https://issues.jenkins-ci.org/browse/JENKINS-1285))
- The cobertura statistics graphic on a project window isn't rendered
    ([JENKINS-2851](https://issues.jenkins-ci.org/browse/JENKINS-2851))

### Version 0.8.4 (21-Oct-2007)

- ???

### Version 0.8.3 (12-Oct-2007)

- Fixes
    [JENKINS-915](https://issues.jenkins-ci.org/browse/JENKINS-915) for
    SubversionSCM only

### Version 0.8.2 (4-Oct-2007)

- Hopefully fixed
    [JENKINS-846](https://issues.jenkins-ci.org/browse/JENKINS-846)

### Version 0.8.1 (28-Sep-2007)

- Fixes issues running under JDK 1.5
- Fixes some issues with finding source code

### Version 0.8 (20-Sep-2007)

- Works with JDK 5 as well as JDK 6 (removing JDK dependency
    introduced during regression fixing)

### Version 0.7 (20-Sep-2007)

- Better fix of regressions introduced in 0.5

### Version 0.6 (20-Sep-2007)

- Fix of regressions introduced in 0.5

### Version 0.5 (20-Sep-2007)

- Now with built in source code painting! (Source code is available at
    the file level for the latest stable build only).

**Note** that the conditional coverage is the highest coverage from all
the cobertura reports aggregated in  each build.  Thus if you have two
reports and one covers only 50% of a conditional and the other covers a
*different* 25%, conditional coverage will be reported as 50% and not
the 75% that you could argue it should be!

- The trend graph now works when there are broken builds in the build
    history.

### Version 0.4 (29-Aug-2007)

- Initial support for multi-report aggregation (may get totals
    incorrect if reports overlap for individual classes - I'll need to
    get source file painting support implemented to remove that issue.
     However, this is just how the files are parsed.  This version will
    archive the files correctly so when it is fixed your history should
    report correctly)

### Version 0.3 (28-Aug-2007)

- Fixed NPE parsing some cobertura reports generated by Cobertura
    version 1.8.
- Project level report should now work (except possibly when a build
    is in progress)

### Version 0.2 (28-Aug-2007)

- Fixed problem with configuration (was not persisting configuration
    details)

&nbsp;

- Changed health reporting configuration (now handles the more generic
    code)

&nbsp;

- Tidy-up of reports

&nbsp;

- Known issues:
  - Project level report does not work in all cases
  - Class and Method level reports should display source code with
    coverage if source code is available (in workspace)

### Version 0.1 (27-Aug-2007)

- Initial release.  Only parses xml report. Some rough edges in the
    UI.
