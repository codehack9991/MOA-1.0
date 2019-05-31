# MOA-1.0 
  Modular Open-source Automation v1.0

    
    
## Problem Statement
      - Lack of automation beyond regression
      - Non-scalable automation solution
      - Disparate maturity of the automation solutions
      - Multiple automation tools, frameworks and utilities across MSST

## MOA-1.0- Quick Start Guide

### Step 1. Set Up Java & Maven & Git Environment
      - 1.Install JDK 1.8 or above version
      - 2.Install Maven 3.3.9 or above version. Set Maven global settings.xml file following guide which I have include in the repository
      - 3.Install Git Bash/Source Tree.
 
### Step 2. Set Up IDE Environment (IntelliJ Idea / Eclipse) 
      - 1.Ensure that JRE System Llibrary is pointed to JDK(not JRE, else there will be build path error).
      - 2.Install Cucumber plugin for IDE. See details.
      - 3.Clone FAST Sample Project to your local directory via Git or FAST Initializr(Recommend). 
          - a. GUI Sample Project
          
### Following (or other versions of them) External Libraries(.jar) are to be included :-
      - 1.commons-lang3-3.0.jar
      - 2.GMDClientAPI-4.0.20.jar
      - 3.hamcrest-core-1.3.jar
      - 4.HdrHistogram-1.2.1.jar
      - 5.ini4j-0.5.2.jar
      - 6.slf4j-api-1.7.5.jar
      - 7.slf4j-log4j12-1.7.5.jar
      - 8.UMS_5.3.1_jdk1.5.0_12-1.0.jar

### Step 3. Run Project
      - 1.Run project in IDE
          Run the project as Maven Build... with goal 'clean install'
      - 2.Run project in command Line
          mvn clean install
          mvn clean verify -DfeaturesDir="features" -DuserName="<USERNAME>" -DenvironmentName="<ENVIRONMENTNAME>" -DforkCount=1 [-            Dtag1="@smoke"] -DcucumberReporting.outputDirectory="../../"

### Step 4. Check Test Report

          The test case report is generated only when the project run with Maven in install phase. A new cucumber report will be generated in <project>/reports folder.
          If you enable the upload dashboard function, you can find your report on FAST Dashboard.
          
### Personal Configuration

          MOA-1.0 allows the user to personalize the configuration, most of them locates on Config folder.
          - 1.User configurationa.Update the Config/config.yml with your own user name.
          - 2.Web Automation configuration (for further details see further below)
          - 3.Email reporting configuration (for further details see further below)
          - 4.JIRA upload configuration (for further details see further below)
          - 5.Dashboard upload configuration 
                - a.Enable this function and update the relative configuration in pom.xml   
                       <projectName>FAST</projectName>
                       <releaseName>Regression</releaseName> 
                       <testSuiteDirectory>${featuresDir}</testSuiteDirectory>
                       <testSuiteName>GUI Examples</testSuiteName>
                       <testType>Regression</testType>
                       <uploadToDashboard>true</uploadToDashboard>
                       
### Web GUI Automation Examples

Go to start of metadata 


      1. Support IE Browser

      Change the config yaml file under the config/environments folder like this :

      WebAgent:

          class_name: 'fast.common.agents.WebBrowserAgent'

          driverClassName: 'org.openqa.selenium.ie.InternetExplorerDriver'

          webDriverPath: drivers/webdriver/IEDriverServer.exe

          webRepo:  repos/web.yml


      2. Support headless mode independent from specific browser


      For now we support IE browser and Chrome browser automation in headless mode, to implement this function just configure the yaml file under the config/environments folder.

      1.IE Browser in headless 

       WebAgent:

          class_name: 'fast.common.agents.WebBrowserAgent'

          headless: yes

          headlessBrowser: ie

          driverClassName: 'org.openqa.selenium.ie.InternetExplorerDriver'

          webDriverPath: drivers/webdriver/IEDriverServer.exe

          webRepo:  repos/web.yml

      We don't have to change the dirverClassName or the webDriverPath in this part, however, sometimes we need to provide a proxy when run the project from an IDE.

      The proxy configuration format is like:

          proxy: Host:Port




      2.Chrome Browser In headless

       WebAgent:

          class_name: 'fast.common.agents.WebBrowserAgent'


          headless: yes

          headlessBrowser: chrome

          driverClassName: 'org.openqa.selenium.chrome.ChromeDriver'

          webDriverPath: drivers/webdriver/chromedriver.exe

          webRepo:  repos/web.yml

      The difference from the IE headless part is that we must point the driverClassName and the webDriverPath to Chrome.





      3. Support configurable options for Google driver 


      User can configure the chrome option when use Chrome browser for web automation both in normal mode and headless mode.

      For example, add this line to the config yaml file under the config/environments folder when add specific argument to chrome.

      WebAgent:

                class_name: 'fast.common.agents.WebBrowserAgent'

        headless: yes

                headlessBrowser: chrome

                driverClassName: 'org.openqa.selenium.chrome.ChromeDriver'

                webDriverPath: drivers/webdriver/chromedriver.exe

        webRepo:  repos/web.yml

        chromeArgument: 'Your specific chrome option argument'

        chromePrefs:

             download.default_directory: 'Your specific chrome prefs'

      4. Support common actions for WEB GUI

      In the 1.2 release version we support press hotkey function for Web GUI automation.

      For example, use can use this glue code in the feature file to implement the press tab action when running a web automation test.

       When WebAgent press "Tab"

      New functions in 1.4 release
      1.Check element does not exist in pagea.Then WebAgent check control SearchButton not exist

      2.Get element attribute valuea.When WebAgent read attribute "name" on control SearchTextbox into @searcheName

      3. Use can customize webdriver download path, see ChromPrefs configuration
      4.Switch to default content from iframea.When WebAgent switch to default content

      5.Add Webdriver forward/refresh actionsa.When WebAgent refresh
      b.When WebAgent forward

      6.Support execution of local javascript filea.When WebAgent generate script "repos\testjs.js" into @jsResult

      7.Add return file name function after screenshota.When WebAgent create screenshot for test case "test download" into @imagePath

      8.Add read text function for web elementa.Then WebAgent read text from downloadZip into @ButtonText

      9.Support combined hot key
      a.When WebAgent press "CONTROL+A" 

      New functions in 1.5 release  

      1. Get current url for the page
            a. Then WebAgent get current url
       2. Enhance counter element

      New functions in 1.6 release  
      1.Check web element whether exists in current page
      2.Accept/Dismiss/Show page alert
      a.Then WebAgent accept alert
      b.Then WebAgent dismiss alert
      c.Then WebAgent show alert

      3.Scroll whole page with width and height offset
      a.When WebAgent scroll current page to width:0 height:500

      4.Get first selected value of selector
      a.Then WebAgent get first selected value from CountrySelector

      5.support right click and double click
      a.When WebAgent right click on DogImage
      b.When WebAgent double click on ResultItem





      New functions in 1.7 release
      1.Support for electron application test
          appBinaryPath: electronApp.exe    adding this new field in web browser agent config will launch electron application instead of chrome browser.

      2.Load page in new tab

             Then WebAgent open "http://www.google.com" url in new tab

      3.Change to next tab 

              Then WebAgent change to next tab

      4.Change to last tab

              Then WebAgent change to last tab

      5.Close current tab

             Then WebAgent close current tab



      New functions in 1.9 release
      1.Support for press Hot Key on control with Index
          a. Assumption locator: functionCode: //input[@id='dynamicIndex']
             WebAgent press  "ENTER" on functionCode with dynamic index "test8"


      2.Support for read Attribute Value with Index

             a. Assumption locator:functionCode: //input[starts-with(@title,'Field') and @name='dynamicIndex']

               WebAgent read attribute "name" on control functionCode with "8" into @transactionNumber

      3.Support for read text from control with Index 

             a. Assumption locator:functionCode: //input[starts-with(@title,'Field') and @name='dynamicIndex']

               WebAgent read text from control functionCode with "8" into @transactionNumber

      4.Support for type Text on a control with Index 

              a. Assumption locator:functionCode: //input[starts-with(@title,'Field') and @name='dynamicIndex']

               WebAgent type "testabc" with dynamic index "8" into  functionCode 






