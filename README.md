# pDES
"pDES" is a project design tool by using Discrete-Event Simulation.

## Description
This software can evaluate a project including product structure, workflow and organization structure by using discrete event simulation. We can define a complex project considering relationship between each component, each task and each team and workers easily by using the interface of pDES. 

Application examples of pDES are as followings:
- Engineering design project
- Heavy industry (Shipbuilding, Steel, etc.)
- Software development project (Waterfall, Agile, Hybrid, etc.)

This software is developed by Eclipse Rich Client Platform (RCP3) and Graphical Editing Framework(GEF3). Simulator is implemented based on previous researches.

## Demo
![Interface](http://www.nakl.t.u-tokyo.ac.jp/~mitsuyuki/pDES/pictures/topInterface.png "topPage")

## Requirement
- Java (1.8 or later)

## Download
Please download pDES from this [URL](http://www.nakl.t.u-tokyo.ac.jp/~mitsuyuki/pDES/download)
	- *Please download and install the latest Java from this [URL](http://www.oracle.com/technetwork/java/javase/downloads/index.html) if necessary.*


## for Developer

0. Download Eclipse
	- ~~Eclipse 4.6(Neon) or latter Eclipse version cannot run this source on 7/25/2016. We will update this solution soon...~~
	- For using this source on pure or old eclipse, you might have to install as following softwares:
		- Eclipse Plug-in Development Environment
		- Eclipse RCP Plug-in Developer Resources
		- Eclipse Graphical Editing Framework (GEF3)

1. Fork it ([http://github.com/taiga4112/pDES/fork](http://github.com/taiga4112/pDES/fork)).

2. Clone pDES project.
	```bash
	$ cd 'yourworkspace'
	$ git clone git@github.com:youraccount/pDES.git
	```

3. Add your explorer of eclipse workspace.
	- Import.. -> Existing Projects into Workspace -> Select pDES project

4. Run
	- Right click on pDES project on Explorer -> Run as -> Eclipse Application

## Contribution
1. Fork it ( http://github.com/taiga4112/pDES/fork )
2. Create your feature branch (git checkout -b my-new-feature)
3. Commit your changes (git commit -am 'Add some feature')
4. Push to the branch (git push origin my-new-feature)
5. Create new Pull Request

## Author

[taiga4112](https://github.com/taiga4112)

