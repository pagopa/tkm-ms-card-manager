## How to start SIT azure pipeline

 1. Move into:
> develop

 1. Run:<br>
    `$version=??` for poweshell or `version=??` for gitbash<br>
 	`mvn --batch-mode release:clean release:prepare`<br>
 	`git checkout -b tmp/${version} card-manager-${version}`<br> 
 	`git push --set-upstream origin tmp/${version}`<br>
 	
 2. Merge **tmp/${version}** into **release/sit**

  ## How to start UAT azure pipeline  
  
 1. Merge **release/sit** into **release/uat**

  ## How to start PROD azure pipeline  
  
 1. Merge **release/uat** into **master**