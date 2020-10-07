
export const navService = {
    fetchNetAssetValueReturns
};

function fetchNetAssetValueReturns(scheme, period, horizon) {
    const reqData = {
        "scheme_no": scheme,
        "investment_period": Number(period),
        "horizon": Number(horizon)
    }
    console.log("reqData = ", reqData);
    const requestOptions = {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Access-Control-Allow-Origin': '*', 'Access-Control-Allow-Headers': '*', 'Access-Control-Allow-Credetials': true, 'Access-Control-Allow-Methods': 'POST' },
        body: JSON.stringify(reqData)
    };

    return fetch(`http://localhost:8080/api/v1/getTrailingReturns`, requestOptions)
        .then(jsonResponse => {
            console.log("Result  is ", jsonResponse)
            return jsonResponse.data;
        });
}