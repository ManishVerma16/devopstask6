FROM  centos:latest

RUN  yum install sudo -y
RUN  yum install  wget -y
RUN  sudo wget -O /etc/yum.repos.d/jenkins.repo http://pkg.jenkins.io/redhat-stable/jenkins.repo  
RUN  sudo rpm --import http://pkg.jenkins.io/redhat-stable/jenkins.io.key 
RUN  yum install jenkins  -y
RUN  yum install java-11-openjdk.x86_64   -y
RUN  sudo  yum install  /sbin/service  -y
RUN  sudo yum install net-tools -y
RUN  sudo yum install python36 -y
RUN  sudo yum install  httpd -y
RUN  sudo yum install  git  -y
RUN  sudo yum install  vim -y
RUN  yum install openssh-clients -y
RUN  curl -LO https://storage.googleapis.com/kubernetes-release/release/`curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt`/bin/linux/amd64/kubectl
RUN  chmod +x kubectl
RUN  mv kubectl  /usr/bin/
RUN  mkdir  /kube_cert/
COPY c*  /kube_cert/
COPY  config /root/.kube/config
COPY  yml /root/yml
RUN sudo yum install ncurses -y
RUN echo -e "jenkins  ALL=(ALL)  NOPASSWD:ALL" >> /etc/sudoers
EXPOSE 8080

CMD java -jar /usr/lib/jenkins/jenkins.war && /bin/bash

#RUN  mkdir  /root/kube/yml
#COPY yml  /root/kube/yml
#sudo /etc/rc.d/init.d/jenkins start  && /bin/bash
#sudo /etc/init.d/jenkins start  && /bin/bash
#sudo service jenkins start


