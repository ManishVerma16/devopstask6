job('Job1_GitClone'){
    description('This job pulls the GitHub repository')
    scm {
            github('ManishVerma16/devopstask6', 'master')
        }
    triggers {
        scm("* * * * *")
    }
    steps {
        shell('sudo cp -rvf * /gitclone')
    }
}

job('Job2_PodLaunch'){
    description('This job will launch the pods after looking the file in repository')

    triggers {
        upstream {
            upstreamProjects('Job1_GitClone')
            threshold('Success')
        } 
    }
    steps {
        shell('''if ls /gitclone/ | grep .html;
then
    if kubectl get deploy | grep web;
    then
        kubectl delete all --all;
        kubectl create -f /gitclone/web-deploy.yml;
        web=$(kubectl get pod -o jsonpath="{.items[0].metadata.name}");
        sleep 15;
        kubectl cp /gitclone/linux.html $web:/var/www/html/;
        kubectl get all;
    else
        kubectl create -f /gitclone/web-deploy.yml;
        kubectl get all;
    fi;
elif ls /gitclone/ | grep .php;
then
    if kubectl get deploy | grep php;
    then
    	kubectl delete all --all;
    	kubectl create -f /gitclone/php-deploy.yml;
   		php=$(kubectl get pod -o jsonpath="{.items[0].metadata.name}");
        sleep 15;
        kubectl cp /gitclone/index.php $php:/var/www/html/; 
        kubectl get all;       
        else
            kubectl create -f /gitclone/php-deploy.yml;
            kubectl get all;
        fi;
else
    echo "error";
fi;''')
   }
}

job('Job3_PodTest'){
    description('This job will test the pods running on Kubernetes')

    triggers {
        upstream {
            upstreamProjects('Job2_PodLaunch')
            threshold('Success')
        } 
    }

    steps {
        shell('''if kubectl get deploy | grep web;
then
	echo "Webserver running well!!!";
elif kubectl get deploy | grep php;
then
	echo "PHP running well!!!";
else
	echo "Pods not working!!!";
fi;''')
    }

    publishers {
        mailer {
    recipients("vermam318@gmail.com")
    notifyEveryUnstableBuild(true)
    sendToIndividuals(false)
        }
    }
}

job('Job4_AppTest'){
    description('This job will test the website running on Webserver in pods')

    triggers {
        upstream {
            upstreamProjects('Job3_PodTest')
            threshold('Success')
        } 
    }
    steps {
        shell('''if kubectl get deploy | grep web;
then
	status=$(curl -s 192.168.99.100:32001/linux.html -w '%{http_code}' -o /dev/null);
	if [ $status -eq 200 ];
    then
    	echo "Webserver running fine";
    else
    	echo "Webserver not running fine";
    fi;
elif kubectl get deploy | grep php;
then
	status=$(curl -s 192.168.99.100:32002/index.php -w '%{http_code}' -o /dev/null);
	if [ $status -eq 200 ];
    then
    	echo "PHP server running fine";
    else
    	echo "PHP not running fine";
    fi;
else
	echo "Pods not running";
fi;
    ''')
    }
}

job('Job5_PodRelaunch'){
    description('This job will looking for the pods if some error found it will again launch')

    triggers {
        upstream {
            upstreamProjects('Job4_PodTest')
            threshold('Success')
        } 
    }

    steps {
        shell('''if kubectl get deploy | grep web;
then
	exit 0;
elif kubectl get deploy | grep php;
then
	exit 0;
else
	echo "Pods not working!!!";
fi;''')
    }
}

buildPipelineView("DevOps_Task_6"){
    title("Jenkins with Kubernetes using Groovy")
    description("This Build Pipeline created for DevOps_Task_6")
    selectedJob("Job1_GitClone")
    alwaysAllowManualTrigger(true)
    displayedBuilds(1)
}