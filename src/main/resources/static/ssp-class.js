var template;
{
    var xhr = new XMLHttpRequest();
    xhr.open('GET', "http://rawgit.com/TheOpenCloudEngine/metaworks4/master/src/main/resources/static/ssp-class.html", false);
    xhr.onload = function () {
        template = xhr.responseText
    }
    xhr.send();
}


Vue.component('ssp-class', {
    template: template,
    props: {
        java: String,
        serviceLocator: Object
    },

    watch:{
      java: function(){
          var initVars = this.initForm();

          this.metadata = initVars.metadata;
      }
    },
    data: function(){

        return this.initForm();

    },

    methods: {
       getServiceHost: function(){
            if(this.serviceLocator){
                if(this.serviceLocator.host){
                    return this.serviceLocator.host;
                }else if(this.$root.$refs[this.serviceLocator]){
                    return this.$root.$refs[this.serviceLocator].host;
                }else{
                    return this.serviceLocator;
                }

            }else{
                return "http://127.0.0.1:8080"
            }
        },

        initForm: function(){

            var classDefinition;
            {

                var xhr = new XMLHttpRequest()
                xhr.open('GET', this.getServiceHost() + "/classdefinition?className=" + this.java, false);
                xhr.setRequestHeader("access_token", localStorage['access_token']);
                xhr.onload = function () {
                    var jsonData = JSON.parse(xhr.responseText)
                    classDefinition = jsonData;
                }
                xhr.send();
            }


            return {
                metadata: classDefinition
            }

        },

    }

})

