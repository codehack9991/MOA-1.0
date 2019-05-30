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

### Step 3. Run Project
      - 1.Run project in IDE
          Run the project as Maven Build... with goal 'clean install'
      - 2.Run project in command Line
          mvn clean install
          mvn clean verify -DfeaturesDir="features" -DuserName="<USERNAME>" -DenvironmentName="<ENVIRONMENTNAME>" -DforkCount=1 [-            Dtag1="@smoke"] -DcucumberReporting.outputDirectory="../../"

### Step 4. Check Test Report

          The test case report is generated only when the project run with Maven in install phase. A new cucumber report will be generated in <project>/reports folder.
          If you enable the upload dashboard function, you can find your report on FAST Dashboard.
