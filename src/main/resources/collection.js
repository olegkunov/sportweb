
var colType = (new URL(location).searchParams).get('type');

switch (colType) {
    case "users": colUrl = "/users"; break;
    case "sports": colUrl = "/sports"; break;
}

var view = new Vue({
    el: '#elements',
    data: {
        title: "",
        elems: [],
        colType: ""
    },
    created() {
        this.colType = colType;
        fetch(colUrl)
            .then ( (response) => { return response.json(); } )
            .then (
                (json) => {
                    this.title = json.title;
                    json.data.forEach (piece => {
                        this.elems.push( piece );
                    } )
                }
            )
    }
});
